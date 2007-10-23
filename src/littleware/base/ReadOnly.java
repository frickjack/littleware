package littleware.base;


import java.lang.annotation.*;

/**
 * Indicates that the annotated method is a read-only method.
 * This annotation should be used for methods on interfaces that may
 * be wrapped by a SessionInvocatioinHandler based dynamic-proxy,
 * so that we can trasnparently give remote-access to the method
 * from a read-only login session.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadOnly { }
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

