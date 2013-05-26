declare var Y:any;
declare var YUI:any;
declare var require:any;

export module toy.button {
	
export class Greeter {
    greeting: string;
    constructor(message: string) {
        this.greeting = message;
    }
    greet() {
        return "Hello, " + this.greeting;
    }
}


//var greeter = new Greeter("world");

export function runGreeter( greeter:Greeter ) {
	var button = document.createElement('button');
	button.innerText = "Say Hello";
	button.onclick = function() {
	    alert(greeter.greet());
	}
	
	document.body.appendChild(button);
}

}


/*
toy.button.runGreeter( new toy.button.Greeter("world") );

YUI.add('littleware-littleUtil', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleUtil = toy.button;
}, '0.1.1' , {
    requires: [ "array-extras" ]
});
*/
