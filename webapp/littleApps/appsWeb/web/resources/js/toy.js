define(["require", "exports"], function(require, exports) {
    (function (toy) {
        (function (button) {
            var Greeter = (function () {
                function Greeter(message) {
                    this.greeting = message;
                }
                Greeter.prototype.greet = function () {
                    return "Hello, " + this.greeting;
                };
                return Greeter;
            })();
            button.Greeter = Greeter;            
            function runGreeter(greeter) {
                var button = document.createElement('button');
                button.innerText = "Say Hello";
                button.onclick = function () {
                    alert(greeter.greet());
                };
                document.body.appendChild(button);
            }
            button.runGreeter = runGreeter;
        })(toy.button || (toy.button = {}));
        var button = toy.button;
    })(exports.toy || (exports.toy = {}));
    var toy = exports.toy;
})
//@ sourceMappingURL=toy.js.map
