/* HarvestException.java - created on 21 de Fev de 2011, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh;

/**
 * A typed exception thrown during OAI-PMH harvesting
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 21 de Fev de 2011
 */
public class HarvestException extends Exception {

    /**
     * For inheritance reasons, pipes through to the super constructor.
     * 
     * @param message
     *            description of the error
     */
    public HarvestException(String message) {
        super(message);
    }

    /**
     * For inheritance reasons, pipes through to the super constructor.
     * 
     * @param message
     *            description of the error
     * @param cause
     *            root cause of the error
     */
    public HarvestException(String message, Throwable cause) {
        super(message, cause);
    }
}
