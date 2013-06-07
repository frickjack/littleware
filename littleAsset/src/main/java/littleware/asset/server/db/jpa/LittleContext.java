/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Mock JNDI context that is just NOOPs except
 * lookup always returns the DataSource injected via
 * the setDataSource static method.
 * Similar to LittleDriver - just a hack to try to get
 * hibernate to use our DataSource
 */
public class LittleContext implements javax.naming.Context {
    private static  DataSource dataSource = null;
    /**
     * HibernateProvider injects littleware data source at startup time as needed
     */
    public static void setDataSource( DataSource value ) {
        dataSource = value;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return dataSource;
    }

    @Override
    public Object lookup(String string) throws NamingException {
        return dataSource;
    }

    @Override
    public void bind(Name name, Object o) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void bind(String string, Object o) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void rebind(Name name, Object o) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void rebind(String string, Object o) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void unbind(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void unbind(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void rename(Name name, Name name1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void rename(String string, String string1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void destroySubcontext(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Context createSubcontext(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object lookupLink(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser("");
    }

    @Override
    public NameParser getNameParser( String string) throws NamingException {
        return new NameParser(){

            @Override
            public Name parse( final String string) throws NamingException {
                return new Name(){
                    @Override
                    public Object clone() { return this; }
                    
                    @Override
                    public int compareTo(Object o) {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public int size() {
                        return 1;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public Enumeration<String> getAll() {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public String get(int i) {
                        return string;
                    }

                    @Override
                    public Name getPrefix(int i) {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Name getSuffix(int i) {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public boolean startsWith(Name name) {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public boolean endsWith(Name name) {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Name addAll(Name name) throws InvalidNameException {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Name addAll(int i, Name name) throws InvalidNameException {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Name add(String string) throws InvalidNameException {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Name add(int i, String string) throws InvalidNameException {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public Object remove(int i) throws InvalidNameException {
                        throw new UnsupportedOperationException("Not supported yet."); 
                    }
                };

            }
        };
    }

    @Override
    public Name composeName(Name name, Name name1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String composeName(String string, String string1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object addToEnvironment(String string, Object o) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object removeFromEnvironment(String string) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void close() throws NamingException {}

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    
    //---------------------------
    
    /**
     * Assign this to the "java.naming.factory.initial" system property
     * to make the LittleContext mock the initial context.
     */
    public static class Factory implements javax.naming.spi.InitialContextFactory {

        @Override
        public Context getInitialContext(Hashtable<?, ?> hshtbl) throws NamingException {
            return new LittleContext();
        }
        
    }
}
