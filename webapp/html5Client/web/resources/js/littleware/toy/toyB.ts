//declare var YUI:any;
/// <reference path="../../libts/yui.d.ts" />
/// <reference path="toyA.ts" />

declare var exports:any;

//declare module littleware.toy.a;
if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-toy-toyB";
}

//import yui = module( "yui" );

var Y:Y.YUI = exports;

import modA = module( "toyA" );

export module littleware.toy.b {
    
    
    //import modA = littleware.toy.a;
    //module a = modA.littleware.toy.a;

  export class Greeter {
      delegate:modA.littleware.toy.a.Greeter;

      constructor(message: string) {
          this.delegate = new modA.littleware.toy.a.Greeter( message );
      }
      greet() {
          return this.delegate.greet();
      }
  }


  //var greeter = new Greeter("world");
  var counter = 0;

  export function runGreeter( greeter:Greeter ) {
      var toyNode = Y.one( "div.toy" );
      var messageNode = toyNode.one( "p" );
      var button = document.createElement('button');
      button.innerText = "Say Hello";
      button.onclick = function() {
          messageNode.setHTML( "" + counter + ": " + greeter.greet()  );
          counter += 1;
      }

      toyNode.append(button);
  }

}


