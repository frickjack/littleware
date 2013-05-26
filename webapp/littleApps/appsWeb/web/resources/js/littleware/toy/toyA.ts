declare var YUI:any;
/// <reference path="yui" />
declare var exports:Y;

if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-toy-toyA";
}

//import yui = module( "yui" );

var Y:Y = exports;

export module littleware.toy.a {
	
export class Greeter {
    greeting: string;
    constructor(message: string) {
        this.greeting = message;
    }
    greet() {
        return "Hello, " + this.greeting;
    }
}



}


