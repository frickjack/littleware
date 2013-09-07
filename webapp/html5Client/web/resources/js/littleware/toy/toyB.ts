declare var exports:any;

//declare module littleware.toy.a;
if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-toy-toyB";
}


import importY = require("../../libts/yui");
importY;
import Y = importY.Y;
Y = exports;

import importModA = require("toyA");
importModA;
import modA = importModA.littleware.toy.a;

// hack - map the module names to the YUI namespace
Y = exports;
modA = exports.littleware.toy.a;

export module littleware.toy.b {
    
    
    //import modA = littleware.toy.a;
    //module a = modA.littleware.toy.a;

  export class Greeter {
      delegate:modA.Greeter;

      constructor(message: string) {
          this.delegate = new modA.Greeter( message );
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


