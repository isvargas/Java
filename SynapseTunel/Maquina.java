/*
 * SynapseTunel - Tunel TCP/IP em Java
 * Ivan S. Vargas
 * contato@is5.com.br
 */
package synapsetunel;

import java.net.Socket;

/**
 *
 * @author Ivan Vargas
 */
public class Maquina {

    private int ID;
    private Socket cliente;

    public void setCliente(Socket cliente) {
        this.cliente = cliente;
    }
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Socket getCliente() {
        return cliente;
    }

    public String getIp() {
        return this.cliente.getInetAddress().getHostAddress();
    }

    public String getNome() {
        return this.cliente.getInetAddress().getHostName();
    }

    public Maquina(Socket client){
        this.cliente = client;        
    }
    
        public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    
    @Override
    public String toString(){
        return this.alias;
    }    
    
}

