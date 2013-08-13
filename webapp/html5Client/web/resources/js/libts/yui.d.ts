/**
 * YUI3 establishes a module namespace root on a variable
 * usually named 'Y' exposed in a closure that the application's
 * main() method runs in.  For example- an application might 
 * launch with code like this: <br />
 *    YUI().use( 'node', function(Y) { ... } );  <br />
 * In this example - the application loads the 'node' YUI module
 * under the Y.Node namespace.
 * A YUI module will often mixin its methods onto the
 * namespace root to save some user typing, 
 * so Y.Node.one() is also available as Y.one().
 * This definition file attempts to provide type 
 * information for commonly used YUI modules including
 * the different function mixins.
 */
module Y {

            function applyTo(id: string, method: string, args: any[]): any;
            function applyConfig(o: any): any;
            function assert(condition: bool, message: string): any;
            function cached(source: Function, cache?: any, refetch?: any): Function;
            function batch(...proms: Promise[]): Promise;
            function bind(f: Function, c: any, args: any): Function;
            function bind(f: string, c: any, args: any): Function;
            function clone(o: any, safe: bool, f: Function, c: any, owner: any, cloned: any): any[];
            function clone(o: any, safe: bool, f: Function, c: any, owner: any, cloned: any): any;
            function aggregate(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[]): any;
            function augment(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any[]): Function;
            function augment(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any): Function;
            function augment(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any[]): Function;
            function augment(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any): Function;
            function all(selector: string): NodeList;
            function after(type: string, fn: Function, context?: any, ...args: any[]): EventHandle;
            function destroy(): any;
            function error(msg: string, e: Error, src: any): YUI;
            function error(msg: string, e: string, src: any): YUI;
            function each(o: any, f: Function, c: any, proto: bool): YUI;
            function extend(r: Function, s: Function, px: any, sx: any): any;
            function dump(o: any, d: number): string;
            function delegate(type: string, fn: Function, el: string, filter: string, context: any, args: any): EventHandle;
            function delegate(type: string, fn: Function, el: string, filter: Function, context: any, args: any): EventHandle;
            function delegate(type: string, fn: Function, el: Node, filter: string, context: any, args: any): EventHandle;
            function delegate(type: string, fn: Function, el: Node, filter: Function, context: any, args: any): EventHandle;
            function instanceOf(o: any, type: any): any;
            function guid(pre: string): string;
            function namespace(namespace: string): any;
            function on(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            function once(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            function onceAfter(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            function message(msg: string, cat: string, src: string, silent: bool): YUI;
            function log(msg: string, cat: string, src: string, silent: bool): YUI;
            function later(when: number, o: any, fn: Function, data: any, periodic: bool): any;
            function later(when: number, o: any, fn: string, data: any, periodic: bool): any;
            function mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            function mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            function mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            function mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            function mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            function mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            function mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            function mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            function mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            function mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            function mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            function mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            function merge(objects: any): any;
            function getLocation(): Location;
            function one(node: string): Node;
            function one(node: HTMLElement): Node;
            function one(node: string): any;
            function one(node: HTMLElement): any;
            function rbind(f: Function, c: any, args: any): Function;
            function rbind(f: string, c: any, args: any): Function;
            function stamp(o: any, readOnly: bool): string;
            function some(o: any, f: Function, c: any, proto: bool): bool;
            function soon(fn: Function): any;
            function substitute(s: string, o: any, f: Function, recurse: bool): string;
            function throttle(fn: Function, ms: number): Function;
            function use(modules: string, callback?: (Y: YUI, status: any) => any): YUI;
            function use(modules: any[], callback?: (Y: YUI, status: any) => any): YUI;
            function when(ref: any): Promise;

    var Assert: Test_Assert;

        interface Anim extends Base { 
            
            
        }
    
        interface App extends App_Base, App_Content, App_Transitions, PjaxContent { 
            (config?: any);
            
            
        }
    
        interface App_Base extends Base, View, Router, PjaxBase { 
            (config?: any);
            
            views: any;
            
            createView(name: string, config?: any): View;
            getViewInfo(view: View): any;
            getViewInfo(view: string): any;
            navigate(url: string, options?: any): any;
            render(): App_Base;
            showView(view: string, config?: any, options?: any, callback?: (view: View) => any): App_Base;
            showView(view: View, config?: any, options?: any, callback?: (view: View) => any): App_Base;
        }
    
        class Array { 
            constructor(thing: any, startIndex?: number, force?: bool);
            
            static each(v: any[], f: (any) => bool, o?: any): any[];
            static filter(v: any[], f: (any) => bool, o?: any): any[];
            static find(v: any[], f: (any) => bool, o?: any): any;
            static reject(v: any[], f: (any) => bool, o?: any): any[];

        }
    
        interface ArrayList { 
            (items: any[]);
            
            
            add(item: any, index: number): ArrayList;
            filter(validator: Function): ArrayList;
            each(fn: Function, context: any): ArrayList;
            isEmpty(): bool;
            indexOf(needle: any): number;
            item(i: number): any;
            itemsAreEqual(a: any, b: any): bool;
            remove(needle: any, all: bool, comparator: Function): ArrayList;
            size(): number;
            some(fn: Function, context: any): bool;
            toJSON(): any[];
        }

        
        interface AsyncQueue extends EventTarget { 
            (callback: Function);
            
            defaults: any;
            
            add(callback: Function): AsyncQueue;
            add(callback: any): AsyncQueue;
            next(): Function;
            isRunning(): bool;
            indexOf(callback: string): number;
            indexOf(callback: Function): number;
            getCallback(id: string): any;
            pause(): AsyncQueue;
            run(): AsyncQueue;
            promote(callback: string): AsyncQueue;
            promote(callback: any): AsyncQueue;
            remove(callback: string): AsyncQueue;
            remove(callback: any): AsyncQueue;
            size(): number;
            stop(): AsyncQueue;
        }
    
        interface AttributeLite { 
            
            
            addAttrs(cfg: any): any;
            get(name: string): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
        }
    
        interface AutoCompleteList extends Widget, AutoCompleteBase, WidgetPosition, WidgetPositionAlign { 
            (config: any);
            
            
            hide(): AutoCompleteList;
            selectItem(itemNode?: Node, originEvent?: EventFacade): AutoCompleteList;
        }
    
        interface Base extends BaseCore, Attribute, AttributeCore, AttributeEvents, AttributeExtras, EventTarget { 
            (config: any);
            
            
            destroy(): Base;
            init(config: any): Base;
        }
    
        interface BaseCore extends AttributeCore { 
            (cfg: any);
            
            name: string;
            
            destroy(): BaseCore;
            init(cfg: any): BaseCore;
            toString(): string;
        }
    
        interface Button extends Widget { 
            (config: any);
            
            
            bindUI(): any;
            syncUI(): any;
        }
    
        interface ButtonCore { 
            (config: any);
            
            
            enable(): any;
            disable(): any;
            getNode(): any;
        }
    
        interface ButtonGroup extends Widget { 
            (config: any);
            
            
            bindUI(): any;
            getButtons(): any;
            getSelectedButtons(): any;
            getSelectedValues(): any;
            renderUI(): any;
        }
    
        interface ButtonPlugin { 
            (config: any);
            
            
            createNode(node: any, config: any): any;
        }
    
        interface Cache extends Base {
            add(request: any, response: any): any;
            retrieve(request: any): any;

            
        }
    
        interface CacheOffline extends Cache { 
            
        }
    
        interface Calendar extends CalendarBase { 
            (config: any);
            
            
            addMonth(): any;
            addYear(): any;
            initializer(): any;
            subtractMonth(): any;
            subtractYear(): any;
        }
    
        interface CalendarBase extends Widget { 
            (config: any);
            
            
            bindUI(): any;
            deselectDates(dates?: Date): any;
            deselectDates(dates?: any[]): any;
            initializer(): any;
            renderUI(): any;
            selectDates(dates: Date): any;
            selectDates(dates: any[]): any;
        }
    
        interface Circle extends Shape { 
            
            
        }
    
        interface Controller extends Base { 
            
            
        }
    
        interface CustomEvent { 
            (type: string, o: any);
            
            bubbles: bool;
            defaultFn: Function;
            async: bool;
            afters: Subscriber ;
            broadcast: number;
            context: any;
            fired: bool;
            firedWith: any[];
            fireOnce: bool;
            emitFacade: bool;
            monitored: bool;
            host: EventTarget;
            prevented: number;
            preventable: bool;
            preventedFn: Function;
            queuable: bool;
            signature: number;
            silent: bool;
            stoppedFn: Function;
            stopped: number;
            subscribers: Subscriber ;
            type: string;
            
            after(fn: Function, context: any, arg: any): EventHandle;
            applyConfig(o: any, force: bool): any;
            detachAll(): number;
            fire(...arguments: Object[]): bool;
            detach(fn: Function, context: any): number;
            hasSubs(): any;
            monitor(what: string): EventHandle;
            getSubs(): any[];
            on(fn: Function, context: any, arg: any): EventHandle;
            log(msg: string, cat: string): any;
            halt(immediate: bool): any;
            preventDefault(): any;
            subscribe(fn: Function): EventHandle;
            stopPropagation(): any;
            stopImmediatePropagation(): any;
            unsubscribe(fn: Function, context: any): number;
            unsubscribe(fn: Function, context: any): undefined;
            unsubscribeAll(): number;
        }
    
        interface DD_DDM extends Base { 
            
            activeDrop: any;
            activeDrag: DD_Drag;
            CSS_PREFIX: string;
            _active: bool;
            otherDrops: any;
            targets: any[];
            validDrops: any[];
            useHash: bool;
            
            clearCache(): any;
            getBestMatch(drops: any[], all: bool): any;
            getBestMatch(drops: any[], all: bool): any[];
            getDrag(node: string): any;
            getDrag(node: any): any;
            getNode(n: Node): Node;
            getNode(n: any): Node;
            getNode(n: string): Node;
            isOverTarget(drop: any): bool;
            getDrop(node: string): any;
            getDrop(node: any): any;
            getDelegate(): any;
            regDelegate(): any;
            stopDrag(): DD_DDM;
            swapPosition(n1: Node, n2: Node): Node;
            swapNode(n1: Node, n2: Node): Node;
            syncActiveShims(force: bool): any;
        }
    
        interface DD_Delegate extends Base { 
            
            dd: any;
            
            createDrop(node: Node, groups: any[]): any;
            syncTargets(): DD_Delegate;
        }
    
        interface DD_Drag extends Base { 
            
            actXY: any[];
            deltaXY: any[];
            nodeXY: any[];
            lastXY: any[];
            mouseXY: any[];
            realXY: any[];
            region: any;
            startXY: any[];
            target: any;
            
            addInvalid(str: string): DD_Drag;
            addHandle(str: string): DD_Drag;
            addToGroup(g: string): DD_Drag;
            end(): DD_Drag;
            removeFromGroup(g: string): DD_Drag;
            removeHandle(str: string): DD_Drag;
            removeInvalid(str: string): DD_Drag;
            start(): DD_Drag;
            stopDrag(): DD_Drag;
            validClick(ev: EventFacade): bool;
        }
    
        interface DD_Drop extends Base { 
            
            overTarget: bool;
            region: any;
            shim: any;
            
            addToGroup(g: string): DD_Drop;
            inGroup(groups: any[]): any;
            removeFromGroup(g: string): DD_Drop;
            sizeShim(): any;
        }
    
        interface DD_Scroll extends Base { 
            
            
            align(): any;
            end(): any;
            start(): any;
        }
    
        interface DataSource_Function extends DataSource_Local { 
            
            
        }
    
        interface DataSource_Get extends DataSource_Local { 
            
            
        }
    
        interface DataSource_IO extends DataSource_Local { 
            
            
        }
    
        interface DataSource_Local extends Base { 
            
            
            sendRequest(request?: any): number;
        }
    
        interface Dial extends Widget { 
            (config: any);
            
            
            syncUI(): any;
        }
    
        interface Do_AlterArgs { 
            (msg: string, newArgs: any[]);
            
            
        }
    
        interface Do_AlterReturn { 
            (msg: string, newRetVal: any);
            
            
        }
    
        interface Do_Error { 
            (msg: string, retVal: any);
            
            
        }
    
        interface Do_Halt { 
            (msg: string, retVal: any);
            
            
        }
    
        interface Do_Method { 
            (obj: any, sFn: any);
            
            
            exec(arg: any): any;
            delete(sid: string, fn: Function, when: string): any;
            register(sid: string, fn: Function, when: string): any;
        }
    
        interface Do_Prevent { 
            (msg: string);
            
            
        }
    
        interface Drawing { 
            
            
            drawRoundRect(x: number, y: number, w: number, h: number, ew: number, eh: number): any;
            clear(): any;
            curveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            end(): any;
            drawRect(x: number, y: number, w: number, h: number): any;
            lineTo(point1: number, point2: number): any;
            moveTo(x: number, y: number): any;
            quadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
        }
    
        interface EditorBase extends Base { 
            
            frame: any;
            
            copyStyles(from: Node, to: Node): any;
            execCommand(cmd: string, val: string): Node;
            execCommand(cmd: string, val: string): NodeList;
            focus(fn: Function): EditorBase;
            getDomPath(node: Node): any;
            getInstance(): YUI;
            hide(): EditorBase;
            getContent(): string;
            render(node: Selector): EditorBase;
            render(node: HTMLElement): EditorBase;
            render(node: Node): EditorBase;
            show(): EditorBase;
        }
    
        interface EditorSelection { 
            
            anchorTextNode: Node;
            anchorOffset: number;
            anchorNode: Node;
            focusNode: Node;
            focusOffset: number;
            focusTextNode: Node;
            isCollapsed: bool;
            text: string;
            
            createRange(): any;
            focusCursor(): Node;
            getCursor(): Node;
            insertAtCursor(html: string, node: Node, offset: number, collapse: bool): Node;
            insertContent(html: string): Node;
            getSelected(): NodeList;
            removeCursor(keep: bool): Node;
            remove(): EditorSelection;
            replace(se: string, re: string): Node;
            setCursor(): Node;
            selectNode(node: Node, collapse: bool): EditorSelection;
            toString(): string;
            wrapContent(tag: string): NodeList;
        }
    
        interface Ellipse extends Shape { 
            
            
        }
    
        interface EventHandle { 
            (evt: CustomEvent, sub: Subscriber);
            
            evt: CustomEvent;
            sub: Subscriber;
            
            detach(): number;
            monitor(what: string): EventHandle;
        }
    
        interface FileFlash extends Base { 
            (config: any);
            
            
            cancelUpload(): any;
            startUpload(url: string, parameters: any, fileFieldName: string): any;
        }
    
        interface FileHTML5 extends Base { 
            (config: any);
            
            
            cancelUpload(): any;
            startUpload(url: string, parameters: any, fileFieldName: string): any;
        }
    
        interface Frame extends Base { 
            
            
            delegate(type: string, fn: Function, cont: string, sel: string): EventHandle;
            focus(fn: Function): Frame;
            hide(): Frame;
            getInstance(): YUI;
            render(node: string): Frame;
            render(node: HTMLElement): Frame;
            render(node: Node): Frame;
            show(): Frame;
            use(): any;
        }
    
        interface Get_Transaction { 
            
            data: any;
            id: number;
            nodes: HTMLElement[];
            options: any;
            requests: any;
            
            abort(msg?: string): any;
            execute(callback: Function): any;
            purge(): any;
        }
    
        interface Graphic { 
            
            
            batch(method: Function): any;
            addShape(cfg: any): any;
            destroy(): any;
            getXY(): any;
            getShapeById(id: string): any;
            removeShape(shape: Shape): any;
            removeShape(shape: string): any;
            removeAllShapes(): any;
        }
    
        interface GraphicBase { 
            (cfg: any);
            
            
        }
    
        interface HistoryHTML5 extends HistoryBase { 
            (config: any);
            
            
        }
    
        interface IO { 
            (config: any);
            
            
            complete(transaction: any, config: any): any;
            error(transaction: any, error: any, config: any): any;
            failure(transaction: any, config: any): any;
            end(transaction: any, config: any): any;
            load(transaction: any, load: any, config: any): any;
            progress(transaction: any, progress: any, config: any): any;
            send(uri: string, config: any, id: number): any;
            setHeader(name: string, value: string): any;
            start(transaction: any, config: any): any;
            success(transaction: any, config: any): any;
            transport(o: any): any;
            xdrResponse(e: string, o: any, c: any): any;
            xdr(uri: string, o: any, c: any): any;
        }
    
        interface ImgLoadGroup extends Base { 
            
            
            addCustomTrigger(name: string, obj: any): ImgLoadGroup;
            addTrigger(obj: any, type: string): ImgLoadGroup;
            fetch(): any;
            registerImage(foo: any): any;
        }
    
        interface ImgLoadImgObj extends Base { 
            
            
            fetch(withinY: number): bool;
        }
    
        interface LazyModelList extends ModelList { 
            
            
            free(model?: Model): LazyModelList;
            free(model?: number): LazyModelList;
            get(name: string): String[];
            getAsHTML(name: string): String[];
            getAsURL(name: string): String[];
            indexOf(needle: Model): number;
            indexOf(needle: any): number;
            reset(models?: Object[], options?: any): LazyModelList;
            reset(models?: Model[], options?: any): LazyModelList;
            reset(models?: ModelList, options?: any): LazyModelList;
            revive(item?: number): Model;
            revive(item?: any): Model;
            revive(item?: number): Model[];
            revive(item?: any): Model[];
            revive(item?: number): any;
            revive(item?: any): any;
            toJSON(): Object[];
        }
    
        interface Loader { 
            (config: any);
            
            async: any;
            allowRollup: bool;
            comboSep: string;
            combine: bool;
            comboBase: string;
            base: string;
            cssAttributes: any;
            charset: string;
            data: any;
            context: any;
            dirty: bool;
            filters: any;
            filter: string;
            force: string[];
            insertBefore: string;
            jsAttributes: any;
            maxURLLength: number;
            ignoreRegistered: any;
            ignore: string[];
            moduleInfo: any;
            loaded: string;
            loadOptional: bool;
            inserted: string;
            patterns: any;
            rollups: any;
            required: string;
            root: string;
            skipped: any;
            sorted: string[];
            skin: any;
            timeout: number;
            
            calculate(o: any, type: string): any;
            addModule(config: any, name?: string): any;
            addGroup(config: any, name: string): any;
            addAlias(use: any[], name: string): any;
            filterRequires(r: any[]): any[];
            formatSkin(skin: string, mod: string): string;
            onSuccess(): any;
            onFailure(): any;
            onCSS(): any;
            onProgress(): any;
            onTimeout(): any;
            getRequires(mod: any): any[];
            isCSSLoaded(name: string): any;
            getProvides(name: string): any;
            getLangPackName(lang: string, mname: string): string;
            getModule(mname: string): any;
            insert(o: any, type: string): any;
            loadNext(mname: string): any;
            load(cb: Function): any;
            resolve(calc?: bool, s?: any[]): any;
            require(what: String[]): any;
            require(...what: string[]): any;
        }
    
        interface Matrix { 
            
            
            decompose(): any;
            deg2rad(deg: number): any;
            applyCSSText(val: string): any;
            multiple(a: number, b: number, c: number, d: number, dx: number, dy: number): any;
            getTransformArray(val: string): any;
            init(config: any): any;
            identity(): any;
            getMatrixArray(): any;
            getContentRect(width: number, height: number, x: number, y: number): any;
            getDeterminant(): any;
            inverse(): any;
            rad2deg(rad: number): any;
            scale(val: number): any;
            rotate(deg: number): any;
            skewY(y: number): any;
            skewX(x: number): any;
            skew(x: number, y: number): any;
            translateY(y: number): any;
            toFilterText(): any;
            toCSSText(): any;
            translate(x: number, y: number): any;
            translateX(x: number): any;
            transpose(): any;
        }
    
        interface Model extends Base { 
            
            changed: any;
            idAttribute: string;
            lastChange: any;
            lists: ModelList[];
            
            destroy(options?: any, callback?: (err: Error) => any): Model;
            generateClientId(): string;
            get(name: string): any;
            getAsHTML(name: string): string;
            getAsURL(name: string): string;
            isModified(): bool;
            isNew(): bool;
            parse(response: any): any;
            set(name: string, value: any, options?: any): Model;
            setAttrs(attributes: any, options?: any): Model;
            toJSON(): any;
            sync(action: string, options?: any, callback?: (err: Error, response?: any) => any): any;
            undo(attrNames?: any[], options?: any): Model;
            validate(attrs: any, callback: (err?: any) => any): any;
        }
    
        interface ModelList extends Base, ArrayList { 
            (config: any);
            
            model: Model;
            
            comparator(model: Model): number;
            comparator(model: Model): string;
            each(callback: (model: Model, index: number, list: ModelList) => any, thisObj?: any): ModelList;
            filter(options?: any, callback?: (model: Model, index: number, list: ModelList) => any): any[];
            filter(options?: any, callback?: (model: Model, index: number, list: ModelList) => any): ModelList;
            get(name: string): any;
            get(name: string): any[];
            getAsHTML(name: string): string;
            getAsHTML(name: string): String[];
            getAsURL(name: string): string;
            getAsURL(name: string): String[];
            getByClientId(clientId: string): Model;
            getById(id: string): Model;
            getById(id: number): Model;
            invoke(name: string, ...args: any[]): any[];
            item(index: number): Model;
            map(fn: (model: Model, index: number, models: Model[]) => any, thisObj?: any): any[];
            parse(response: any): Object[];
            some(callback: (model: Model, index: number, list: ModelList) => any, thisObj?: any): bool;
            sort(options?: any): ModelList;
            toJSON(): Object[];
            toArray(): any[];
            sync(action: string, options?: any, callback?: (err: Error, response?: any) => any): any;
        }
    
        interface Node extends EventTarget { 
            (node: HTMLElement);
            
            contains(needle: Node): bool;
            contains(needle: HTMLElement): bool;
            createCaption(): Node;
            blur(): Node;
            cloneNode(deep: bool): Node;
            clearData(name: string): Node;
            appendTo(node: Node): Node;
            appendTo(node: HTMLElement): Node;
            appendChild(node: string): Node;
            appendChild(node: HTMLElement): Node;
            appendChild(node: Node): Node;
            append(content: string): Node;
            append(content: Node): Node;
            append(content: HTMLElement): Node;
            all(selector: string): NodeList;
            ancestors(fn: string, testSelf: bool): NodeList;
            ancestors(fn: Function, testSelf: bool): NodeList;
            ancestor(fn: string, testSelf: bool, stopFn: string): Node;
            ancestor(fn: string, testSelf: bool, stopFn: Function): Node;
            ancestor(fn: Function, testSelf: bool, stopFn: string): Node;
            ancestor(fn: Function, testSelf: bool, stopFn: Function): Node;
            compareTo(refNode: HTMLElement): bool;
            compareTo(refNode: Node): bool;
            addClass(className: string): Node;
            generateID(): string;
            getAttribute(name: string): string;
            focus(): Node;
            delegate(type: string, fn: Function, el: string): EventHandle;
            delegate(type: string, fn: Function, spec: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, spec: Function, context: any, args: any): EventHandle;
            each(fn: Function, context: any): Node;
            empty(): Node;
            destroy(recursivePurge: bool): any;
            getAttrs(attrs: any[]): any;
            get(attr: string): any;
            hide(name: string, config: any, callback: Function): Node;
            hide();
            getComputedStyle(attr: string): string;
            getStyle(attr: string): string;
            getY(): number;
            getX(): number;
            getXY(): any[];
            inRegion(node2: Node, all: bool, altRegion: any): any;
            inRegion(node2: any, all: bool, altRegion: any): any;
            intersect(node2: Node, altRegion: any): any;
            intersect(node2: any, altRegion: any): any;
            load(url: string, selector: string, callback: Function): Node;
            getElementsByTagName(tagName: string): NodeList;
            hasAttribute(attribute: string): bool;
            hasChildNodes(): bool;
            on(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            one(node: string): Node;
            one(node: HTMLElement): Node;
            one(node: string): any;
            one(node: HTMLElement): any;                        
            item(index: number): Node;
            getData(name: string): any;
            getHTML(): string;
            getContent(): string;
            insertBefore(newNode: string, refNode: HTMLElement): Node;
            insertBefore(newNode: string, refNode: Node): Node;
            insertBefore(newNode: HTMLElement, refNode: HTMLElement): Node;
            insertBefore(newNode: HTMLElement, refNode: Node): Node;
            insertBefore(newNode: Node, refNode: HTMLElement): Node;
            insertBefore(newNode: Node, refNode: Node): Node;
            insert(content: string, where: number): Node;
            insert(content: string, where: Node): Node;
            insert(content: string, where: HTMLElement): Node;
            insert(content: string, where: string): Node;
            insert(content: Node, where: number): Node;
            insert(content: Node, where: Node): Node;
            insert(content: Node, where: HTMLElement): Node;
            insert(content: Node, where: string): Node;
            insert(content: HTMLElement, where: number): Node;
            insert(content: HTMLElement, where: Node): Node;
            insert(content: HTMLElement, where: HTMLElement): Node;
            insert(content: HTMLElement, where: string): Node;
            insert(content: NodeList, where: number): Node;
            insert(content: NodeList, where: Node): Node;
            insert(content: NodeList, where: HTMLElement): Node;
            insert(content: NodeList, where: string): Node;
            insert(content: HTMLCollection, where: number): Node;
            insert(content: HTMLCollection, where: Node): Node;
            insert(content: HTMLCollection, where: HTMLElement): Node;
            insert(content: HTMLCollection, where: string): Node;
            invoke(method: string, a: any): any;
            next(fn: string): Node;
            next(fn: Function): Node;
            inDoc(doc: Node): bool;
            inDoc(doc: HTMLElement): bool;
            hasClass(className: string): bool;
            prepend(content: string): Node;
            prepend(content: Node): Node;
            prepend(content: HTMLElement): Node;
            previous(fn: string): Node;
            previous(fn: Function): Node;
            remove(destroy: bool): Node;
            replace(newNode: Node): Node;
            replace(newNode: HTMLElement): Node;
            replaceChild(node: string, refNode: HTMLElement): Node;
            replaceChild(node: string, refNode: Node): Node;
            replaceChild(node: HTMLElement, refNode: HTMLElement): Node;
            replaceChild(node: HTMLElement, refNode: Node): Node;
            replaceChild(node: Node, refNode: HTMLElement): Node;
            replaceChild(node: Node, refNode: Node): Node;
            query(selector: string): Node;
            queryAll(selector: string): NodeList;
            purge(recurse: bool, type: string): Node;
            removeChild(node: HTMLElement): Node;
            removeChild(node: Node): Node;
            scrollIntoView(): Node;
            reset(): Node;
            removeAttribute(attribute: string): Node;
            replaceClass(oldClassName: string, newClassName: string): Node;
            removeClass(className: string): Node;
            set(attr: string, val: any): Node;
            setAttrs(attrMap: any): Node;
            siblings(fn: string): NodeList;
            siblings(fn: Function): NodeList;
            setContent(content: string): Node;
            setContent(content: Node): Node;
            setContent(content: HTMLElement): Node;
            setContent(content: NodeList): Node;
            setContent(content: HTMLCollection): Node;
            setHTML(content: string): Node;
            setHTML(content: Node): Node;
            setHTML(content: HTMLElement): Node;
            setHTML(content: NodeList): Node;
            setHTML(content: HTMLCollection): Node;
            setData(name: string, val: any): Node;
            size(): number;
            simulate(type: string, options: any): undefined;
            simulateGesture(name: string, options?: any, cb?: (err: Error) => any): undefined;
            select(): Node;
            setAttribute(name: string, value: string): Node;
            setXY(xy: any[]): Node;
            setX(x: number): Node;
            setY(y: number): Node;
            setStyle(attr: string, val: string): Node;
            setStyle(attr: string, val: number): Node;
            setStyles(hash: any): Node;
            show(name: string, config: any, callback: Function): Node;
            show();
            toString(): string;
            toggleClass(className: string, force: bool): Node;
            transition(config: any, callback: Function): Node;
            test(selector: string): bool;
            swap(otherNode: Node): Node;
            toggleView(on?: bool, callback?: Function): Node;
            swapXY(otherNode: Node): Node;
            swapXY(otherNode: HTMLElement): Node;
            submit(): Node;
            unwrap(): Node;
            wrap(html: string): Node;
        }
    
        interface NodeList { 
            (nodes: string);
            
            
            concat(valueN: NodeList): NodeList;
            concat(valueN: any[]): NodeList;
            after(type: string, fn: Function, context: any): EventHandle;
            clearData(name: string): NodeList;
            appendChild(): any;
            append(): any;
            addClass(className: string): NodeList;
            get(): any;
            empty(): NodeList;
            destroy(recursivePurge: bool): any;
            even(): NodeList;
            filter(selector: string): NodeList;
            each(fn: Function, context: any): NodeList;
            generateID(): string;
            detachAll(): any;
            detach(): any;
            each( fn: Function ): any;
            getAttribute(name: string): string;
            isEmpty(): bool;
            odd(): NodeList;
            modulus(n: number, r: number): NodeList;
            indexOf(node: Node): number;
            indexOf(node: HTMLElement): number;
            item(index: number): Node;
            hide(name: string, config: any, callback: Function): NodeList;
            getComputedStyle(attr: string): any[];
            getStyle(attr: string): any[];
            onceAfter(type: string, fn: Function, context: any): EventHandle;
            once(type: string, fn: Function, context: any): EventHandle;
            on(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            getData(name: string): any[];
            getHTML(): any;
            getContent(): any;
            insertBefore(): any;
            insert(): any;
            hasClass(className: string): any[];
            pop(): Node;
            plug(P: Function, config: any): NodeList;
            plug(P: any, config: any): NodeList;
            plug(P: any[], config: any): NodeList;
            prepend(): any;
            replaceClass(oldClassName: string, newClassName: string): NodeList;
            removeAttribute(name: string): any;
            removeClass(className: string): NodeList;
            push(nodes: Node): any;
            push(nodes: HTMLElement): any;
            refresh(): NodeList;
            remove(destroy: bool): NodeList;
            setAttribute(name: string, value: string): NodeList;
            setContent(): any;
            setHTML(): any;
            setData(name: string, val: any): NodeList;
            setStyle(attr: string, val: string): NodeList;
            setStyle(attr: string, val: number): NodeList;
            setStyles(hash: any): NodeList;
            show(name: string, config: any, callback: Function): NodeList;
            shift(): Node;
            slice(begin: number, end: number): NodeList;
            splice(index: number, howMany: number): NodeList;
            some(fn: Function, context: any): bool;
            size(): number;
            set(attr: string, val: any): NodeList;
            toFrag(): Node;
            transition(config: any, callback: Function): NodeList;
            toggleView(on?: bool, callback?: Function): NodeList;
            toggleClass(className: string): NodeList;
            unwrap(): NodeList;
            unshift(nodes: Node): any;
            unshift(nodes: HTMLElement): any;
            unplug(plugin: string): NodeList;
            unplug(plugin: Function): NodeList;
            wrap(html: string): NodeList;
        }
    
        interface Overlay extends Widget, WidgetStdMod, WidgetPosition, WidgetStack, WidgetPositionAlign, WidgetPositionConstrain { 
            (object: any);
            
            
        }
    
        interface Panel extends Widget, WidgetAutohide, WidgetButtons, WidgetModality, WidgetPosition, WidgetPositionAlign, WidgetPositionConstrain, WidgetStack, WidgetStdMod { 
            
            BUTTONS: any;
            
        }
    
        interface Path extends Shape, Drawing { 
            
            
        }
    
        interface Pjax extends Router, PjaxBase, PjaxContent { 
            (config?: any);
            
            
        }
    
        interface Plugin_DDConstrained extends Base { 
            
            
            align(): any;
            drag(): any;
            getRegion(inc: bool): any;
            inRegion(xy: any[]): bool;
            resetCache(): any;
        }
    
        
        interface Plugin_DDNodeScroll { 
            
            
        }
    
        interface Plugin_DDProxy extends Base { 
            
            
        }
    
        interface Plugin_DDWindowScroll { 
            
            
        }
    
        interface Plugin_Drag extends DD_Drag { 
            
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_Drop extends DD_Drop { 
            
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_EditorBR extends Base { 
            
            
        }
    
        interface Plugin_EditorBidi extends Base { 
            
            
        }
    
        interface Plugin_EditorLists extends Base { 
            
            
        }
    
        interface Plugin_EditorPara extends Plugin_EditorParaBase { 
            
            
        }
    
        interface Plugin_EditorParaBase extends Base { 
            
            
        }
    
        interface Plugin_EditorParaIE extends Plugin_EditorParaBase { 
            
            
        }
    
        interface Plugin_EditorTab extends Base { 
            
            
        }
    
        interface Plugin_ExecCommand extends Base { 
            
            bidi: any;
            
            command(action: string, value: string): Node;
            command(action: string, value: string): NodeList;
            getInstance(): YUI;
        }
    
        interface Plugin_ResizeProxy extends Plugin_Base { 
            
            PROXY_TEMPLATE: string;
            
        }
    
        interface Plugin_ScrollViewList extends Plugin_Base { 
            
            
            initializer(): any;
        }
    
        interface Plugin_ScrollViewPaginator extends Plugin_Base { 
            
            
            next(): any;
            initializer(Configuration: any): any;
            prev(): any;
            scrollToIndex(index: number, duration?: number, easing?: string): any;
        }
    
        interface Plugin_ScrollViewScrollbars extends Plugin_Base { 
            
            
            flash(): any;
            hide(animated: bool): any;
            initializer(): any;
            show(animated: bool): any;
        }
    
        interface Plugin_SortScroll extends Base { 
            
            
        }
    
        interface Queue { 
            (item: any);
            
            
            add(item: any): any;
            last(): any;
            next(): any;
            indexOf(needle: any): number;
            promote(item: any): any;
            remove(item: any): any;
            size(): number;
        }
    
        interface Recordset extends Base, ArrayList { 
            (config: any);
            
            
            _setRecords(items: Record[]): Record[];
            _setRecords(items: Object[]): Record[];
            getValuesByKey(key?: string): any[];
            getLength(): number;
            getRecordsByIndex(index: number, range: number): any[];
            getRecordByIndex(i: number): Record;
            getRecord(i: String, Number): Record;
        }
    
        interface Rect extends Shape { 
            
            
        }
    
        interface Resize extends Base { 
            (config: any);
            
            changeWidthHandles: bool;
            changeTopHandles: bool;
            changeLeftHandles: bool;
            changeHeightHandles: bool;
            HANDLE_TEMPLATE: string;
            WRAP_TEMPLATE: string;
            HANDLES_WRAP_TEMPLATE: string;
            REGEX_CHANGE_WIDTH: string;
            REGEX_CHANGE_TOP: string;
            REGEX_CHANGE_LEFT: string;
            REGEX_CHANGE_HEIGHT: string;
            ALL_HANDLES: string;
            delegate: any;
            nodeSurrounding: any;
            totalHSurrounding: number;
            totalVSurrounding: number;
            wrapperSurrounding: any;
            
            eachHandle(fn: Function): any;
        }
    
        interface Router extends Base { 
            (config?: any);
            
            
            dispatch(): Router;
            getPath(): string;
            hasRoute(url: string): bool;
            match(path: string): Object[];
            removeRoot(url: string): string;
            removeQuery(url: string): string;
            replace(url?: string): Router;
            route(path: string, callbacks: (req: any, res: any, next: (err?: string) => any) => any): Router;
            save(url?: string): Router;
            upgrade(): bool;
        }
    
        interface SWF { 
            (id: string, swfURL: string, p_oAttributes: any);
            
            
            callSWF(func: string, args: any[]): any;
            toString(): string;
        }
    
        interface SWFDetect { 
            
            
            isFlashVersionAtLeast(flashMajor: number, flashMinor: number, flashRev: number): bool;
            getFlashVersion(): any;
        }
    
        interface ScrollView extends Widget { 
            (config: any);
            
            lastScrolledAmt: number;
            
            bindUI(): any;
            initializer(Configuration: any): any;
            scrollTo(x: number, y: number, duration?: number, easing?: string, node?: string): any;
            syncUI(): any;
        }
    
        interface Shape { 
            (cfg: any);
            
            
            compareTo(refNode: HTMLElement): bool;
            compareTo(refNode: Node): bool;
            contains(needle: Shape): any;
            contains(needle: HTMLElement): any;
            addClass(className: string): any;
            destroy(): any;
            getXY(): any;
            getBounds(): any;
            removeClass(className: string): any;
            rotate(deg: number): any;
            scale(val: number): any;
            setXY(Contains: any[]): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
            skew(x: number, y: number): any;
            skewX(x: number): any;
            skewY(y: number): any;
            translateY(y: number): any;
            translateX(x: number): any;
            translate(x: number, y: number): any;
            test(selector: string): any;
        }
    
        interface ShapeGroup { 
            
            
        }
    
        interface Slider extends SliderBase, SliderValueRange, ClickableRail { 
            (config: any);
            
            
        }
    
        interface SliderBase extends Widget { 
            (config: any);
            
            THUMB_TEMPLATE: string;
            RAIL_TEMPLATE: string;
            CONTENT_TEMPLATE: string;
            BOUNDING_TEMPLATE: string;
            rail: Node;
            thumb: Node;
            
            renderThumb(): Node;
            renderRail(): Node;
            syncUI(): any;
        }
    
        interface Sortable extends Base { 
            
            drop: DD_Drop;
            delegate: DD_Delegate;
            
            getOrdering(Function: any): any;
            join(Sortable: any, String: any): Sortable;
            plug(Class: any, Object: any): Sortable;
            sync(): Sortable;
        }
    
        interface State { 
            
            data: any;
            
            add(name: string, key: string, val: any): any;
            addAll(name: string, obj: any): any;
            get(name: string, key: string): any;
            getAll(name: string, reference: bool): any;
            remove(name: string, key: string): any;
            removeAll(name: string, obj: any): any;
            removeAll(name: string, obj: any[]): any;
        }
    
        interface StyleSheet { 
            (seed: string, name: string);
            
            
            disable(): StyleSheet;
            enable(): StyleSheet;
            getCssText(sel: string): string;
            isEnabled(): bool;
            getId(): number;
            set(sel: string, css: any): StyleSheet;
            unset(sel: string, css: string): StyleSheet;
            unset(sel: string, css: any[]): StyleSheet;
        }
    
        interface Subscriber { 
            (fn: Function, context: any, args: any[]);
            
            args: any[];
            context: any;
            events: EventTarget;
            fn: Function;
            id: string;
            once: any;
            
            contains(fn: Function, context: any): bool;
            notify(args: any[], ce: CustomEvent): any;
        }
    
        interface SynthRegistry { 
            (el: HTMLElement, yuid: string, key: string);
            
            
            _unregisterSub(sub: any): any;
            register(handle: EventHandle): any;
        }
    
        interface SyntheticEvent { 
            (cfg: any);
            
            preventDups: bool;
            
            applyArgExtras(extra: any, sub: any): any;
            detachDelegate(node: Node, sub: any, notifier: SyntheticEvent_Notifier, filter: string): any;
            detachDelegate(node: Node, sub: any, notifier: SyntheticEvent_Notifier, filter: Function): any;
            delegate(node: Node, sub: any, notifier: SyntheticEvent_Notifier, filter: string): any;
            delegate(node: Node, sub: any, notifier: SyntheticEvent_Notifier, filter: Function): any;
            detach(node: Node, sub: any, notifier: SyntheticEvent_Notifier): any;
            on(node: Node, sub: any, notifier: SyntheticEvent_Notifier): any;
            getSubs(node: Node, args: any[], filter: Function, first: bool): EventHandle[];
            processArgs(args: any[], delegate: bool): any;
            subMatch(sub: any, args: any[]): bool;
        }
    
        interface SyntheticEvent_Notifier { 
            (handle: EventHandle, emitFacade: bool);
            
            
        }
    
        interface Tab extends Widget, WidgetChild { 
            (config: any);
            
            
        }
    
        interface TabView extends Widget, WidgetParent { 
            (config: any);
            
            
        }
    
        interface Test_Reporter { 
            (url: string, format: Function);
            
            format: Function;
            url: string;
            
            clearFields(): undefined;
            addField(name: string, value: any): undefined;
            destroy(): undefined;
            report(results: any): undefined;
        }
    
        interface ToggleButton extends Button { 
            (config: any);
            
            
            bindUI(): any;
            toggle(): any;
            syncUI(): any;
        }
    
        interface Transition { 
            
            
        }
    
        interface UploaderFlash extends Widget { 
            (config: any);
            
            queue: Uploader_Queue;
            
            upload(file: FileFlash, url: string, postVars: any): any;
            uploadAll(url: string, postVars: any): any;
            uploadThese(files: any[], url: string, postVars: any): any;
        }
    
        interface UploaderHTML5 extends Widget { 
            
            queue: Uploader_Queue;
            
            openFileSelectDialog(): any;
            uploadThese(files: any[], url: string, postVars: any): any;
            uploadAll(url: string, postVars: any): any;
            upload(file: File, url: string, postVars: any): any;
        }
    
        interface Uploader_Queue extends Base { 
            (config: any);
            
            bytesTotal: number;
            bytesUploaded: number;
            fileList: number;
            errorAction: string;
            simUploads: number;
            
            cancelUpload(file: File): any;
            addToQueueBottom(file: File): any;
            addToQueueTop(file: File): any;
            forceReupload(file: File): any;
            pauseUpload(): any;
            restartUpload(): any;
            startUpload(): any;
        }
    
        interface View extends Base { 
            
            containerTemplate: string;
            events: any;
            template: any;
            
            attachEvents(events?: any): View;
            create(container?: HTMLElement): Node;
            create(container?: Node): Node;
            create(container?: string): Node;
            destroy(options?: any): View;
            detachEvents(): View;
            remove(): View;
            render(): View;
        }
    
        interface Widget extends Base { 
            UI_EVENTS: any;
            BOUNDING_TEMPLATE: string;
            CONTENT_TEMPLATE: string;
            DEF_PARENT_NODE: string;
            DEF_UNIT: string;
            
            blur(): Widget;
            ancestor(depth: number): Widget;
            disable(): Widget;
            enable(): Widget;
            focus(): Widget;
            destroy(destroyAllNodes: bool): Widget;
            getSkinName(): string;
            getDefaultLocale(): string;
            getStrings(key: string): string;
            getString(key: string): string;
            hide(): Widget;
            getClassName(...args: String[]): any;
            isRoot(): bool;
            next(circular: bool): Widget;
            previous(circular: bool): Widget;
            show(): Widget;
            toString(): string;
        }
    
        interface WidgetParent extends ArrayList { 
            (config: any);
            
            
            add(child: Widget, index: number): ArrayList;
            add(child: any, index: number): ArrayList;
            deselectAll(): any;
            destructor(): any;
            removeAll(): ArrayList;
            remove(index: number): Widget;
            selectAll(): any;
            selectChild(i: number): any;
        }
    
        interface WidgetPositionAlign { 
            (config: any);
            
            
        }
    
        interface YQL { 
            (sql: string, callback: Function, params: any, opts: any);
            
            
        }
    
        interface YQLRequest { 
            (sql: string, callback: Function, params: any, opts: any);
            
            
            send(): YQLRequest;
        }
    
        
        interface YUI extends EventTarget { 
            (o?: any);
            
            Anim: AnimStatic;
            App: AppStatic;
            App_Base: App_BaseStatic;
            ArrayList: ArrayListStatic;
            Array: ArrayStatic;
            Assert: Test_Assert;
            AsyncQueue: AsyncQueueStatic;
            AttributeLite: AttributeLiteStatic;
            AutoCompleteList: AutoCompleteListStatic;
            BaseCore: BaseCoreStatic;
            Base: BaseStatic;
            ButtonCore: ButtonCoreStatic;
            ButtonGroup: ButtonGroupStatic;
            ButtonPlugin: ButtonPluginStatic;
            Button: ButtonStatic;
            CacheOffline: CacheOfflineStatic;
            Cache: CacheStatic;
            CalendarBase: CalendarBaseStatic;
            Calendar: CalendarStatic;
            Circle: CircleStatic;
            Controller: ControllerStatic;
            CustomEvent: CustomEventStatic;
            DD_DDM: DD_DDMStatic;
            DD_Delegate: DD_DelegateStatic;
            DD_Drag: DD_DragStatic;
            DD_Drop: DD_DropStatic;
            DD_Scroll: DD_ScrollStatic;
            DataSource_Function: DataSource_FunctionStatic;
            DataSource_Get: DataSource_GetStatic;
            DataSource_IO: DataSource_IOStatic;
            DataSource_Local: DataSource_LocalStatic;
            Dial: DialStatic;
            Do_AlterArgs: Do_AlterArgsStatic;
            Do_AlterReturn: Do_AlterReturnStatic;
            Do_Error: Do_ErrorStatic;
            Do_Halt: Do_HaltStatic;
            Do_Method: Do_MethodStatic;
            Do_Prevent: Do_PreventStatic;
            Drawing: DrawingStatic;
            EditorBase: EditorBaseStatic;
            EditorSelection: EditorSelectionStatic;
            Ellipse: EllipseStatic;
            EventHandle: EventHandleStatic;
            FileFlash: FileFlashStatic;
            FileHTML5: FileHTML5Static;
            Frame: FrameStatic;
            Get_Transaction: Get_TransactionStatic;
            GraphicBase: GraphicBaseStatic;
            Graphic: GraphicStatic;
            HistoryHTML5: HistoryHTML5Static;
            IO: IOStatic;
            ImgLoadGroup: ImgLoadGroupStatic;
            ImgLoadImgObj: ImgLoadImgObjStatic;
            LazyModelList: LazyModelListStatic;
            Loader: LoaderStatic;
            Matrix: MatrixStatic;
            ModelList: ModelListStatic;
            Model: ModelStatic;
            NodeList: NodeListStatic;
            Node: NodeStatic;
            Overlay: OverlayStatic;
            Panel: PanelStatic;
            Path: PathStatic;
            Pjax: PjaxStatic;
            Plugin_DDConstrained: Plugin_DDConstrainedStatic;
            Plugin_DDNodeScroll: Plugin_DDNodeScrollStatic;
            Plugin_DDProxy: Plugin_DDProxyStatic;
            Plugin_DDWindowScroll: Plugin_DDWindowScrollStatic;
            Plugin_Drag: Plugin_DragStatic;
            Plugin_Drop: Plugin_DropStatic;
            Plugin_EditorBR: Plugin_EditorBRStatic;
            Plugin_EditorBidi: Plugin_EditorBidiStatic;
            Plugin_EditorLists: Plugin_EditorListsStatic;
            Plugin_EditorParaBase: Plugin_EditorParaBaseStatic;
            Plugin_EditorParaIE: Plugin_EditorParaIEStatic;
            Plugin_EditorPara: Plugin_EditorParaStatic;
            Plugin_EditorTab: Plugin_EditorTabStatic;
            Plugin_ExecCommand: Plugin_ExecCommandStatic;
            Plugin_ResizeProxy: Plugin_ResizeProxyStatic;
            Plugin_ScrollViewList: Plugin_ScrollViewListStatic;
            Plugin_ScrollViewPaginator: Plugin_ScrollViewPaginatorStatic;
            Plugin_ScrollViewScrollbars: Plugin_ScrollViewScrollbarsStatic;
            Plugin_SortScroll: Plugin_SortScrollStatic;
            Queue: QueueStatic;
            Recordset: RecordsetStatic;
            Rect: RectStatic;
            Resize: ResizeStatic;
            Router: RouterStatic;
            SWFDetect: SWFDetectStatic;
            SWF: SWFStatic;
            ScrollView: ScrollViewStatic;
            ShapeGroup: ShapeGroupStatic;
            Shape: ShapeStatic;
            SliderBase: SliderBaseStatic;
            Slider: SliderStatic;
            Sortable: SortableStatic;
            State: StateStatic;
            StyleSheet: StyleSheetStatic;
            Subscriber: SubscriberStatic;
            SynthRegistry: SynthRegistryStatic;
            SyntheticEvent: SyntheticEventStatic;
            SyntheticEvent_Notifier: SyntheticEvent_NotifierStatic;
            Tab: TabStatic;
            TabView: TabViewStatic;
            Test_Reporter: Test_ReporterStatic;
            ToggleButton: ToggleButtonStatic;
            Transition: TransitionStatic;
            UploaderFlash: UploaderFlashStatic;
            UploaderHTML5: UploaderHTML5Static;
            Uploader_Queue: Uploader_QueueStatic;
            View: ViewStatic;
            WidgetParent: WidgetParentStatic;
            WidgetPositionAlign: WidgetPositionAlignStatic;
            Widget: WidgetStatic;
            YQLRequest: YQLRequestStatic;
            YQL: YQLStatic;
            App_Content: App_Content;
            App_Transitions: App_Transitions;
            App_TransitionsNative: App_TransitionsNative;
            AreaSeries: AreaSeries;
            AreaSplineSeries: AreaSplineSeries;
            ArraySort: ArraySort;
            Attribute: Attribute;
            AttributeCore: AttributeCore;
            AttributeEvents: AttributeEvents;
            AttributeExtras: AttributeExtras;
            AutoComplete: AutoComplete;
            AutoCompleteBase: AutoCompleteBase;
            AutoCompleteFilters: AutoCompleteFilters;
            AutoCompleteHighlighters: AutoCompleteHighlighters;
            Axis: Axis;
            AxisType: AxisType;
            BarSeries: BarSeries;
            BottomAxisLayout: BottomAxisLayout;
            CanvasCircle: CanvasCircle;
            CanvasDrawing: CanvasDrawing;
            CanvasEllipse: CanvasEllipse;
            CanvasGraphic: CanvasGraphic;
            CanvasPath: CanvasPath;
            CanvasPieSlice: CanvasPieSlice;
            CanvasRect: CanvasRect;
            CanvasShape: CanvasShape;
            CartesianChart: CartesianChart;
            CartesianSeries: CartesianSeries;
            CategoryAxis: CategoryAxis;
            Chart: Chart;
            ChartBase: ChartBase;
            ChartLegend: ChartLegend;
            CircleGroup: CircleGroup;
            ClassNameManager: ClassNameManager;
            ClickableRail: ClickableRail;
            ColumnSeries: ColumnSeries;
            ComboSeries: ComboSeries;
            ComboSplineSeries: ComboSplineSeries;
            Console: Console;
            Cookie: Cookie;
            CurveUtil: CurveUtil;
            DD: DD;
            DOM: DOM;
            DOMEventFacade: DOMEventFacade;
            DataSchema: DataSchema;
            DataSchema_Array: DataSchema_Array;
            DataSchema_Base: DataSchema_Base;
            DataSchema_JSON: DataSchema_JSON;
            DataSchema_Text: DataSchema_Text;
            DataSchema_XML: DataSchema_XML;
            DataSource: DataSource;
            DataSourceArraySchema: DataSourceArraySchema;
            DataSourceCache: DataSourceCache;
            DataSourceCacheExtension: DataSourceCacheExtension;
            DataSourceJSONSchema: DataSourceJSONSchema;
            DataSourceTextSchema: DataSourceTextSchema;
            DataSourceXMLSchema: DataSourceXMLSchema;
            DataTable: DataTable;
            DataTable_Base: DataTable_Base;
            DataTable_BodyView: DataTable_BodyView;
            DataTable_ColumnWidths: DataTable_ColumnWidths;
            DataTable_Core: DataTable_Core;
            DataTable_HeaderView: DataTable_HeaderView;
            DataTable_Message: DataTable_Message;
            DataTable_Mutable: DataTable_Mutable;
            DataTable_Scrollable: DataTable_Scrollable;
            DataTable_Sortable: DataTable_Sortable;
            DataTable_TableView: DataTable_TableView;
            Date: Date;
            Do: Do;
            Easing: Easing;
            EllipseGroup: EllipseGroup;
            Escape: Escape;
            Event: Event;
            EventFacade: EventFacade;
            EventTarget: EventTarget;
            ExecCommand: ExecCommand;
            Features: Features;
            File: File;
            Fills: Fills;
            Get: Get;
            GetNodeJS: GetNodeJS;
            Graph: Graph;
            Gridlines: Gridlines;
            GroupDiamond: GroupDiamond;
            GroupRect: GroupRect;
            Handlebars: Handlebars;
            Highlight: Highlight;
            Histogram: Histogram;
            HistoryBase: HistoryBase;
            HistoryHash: HistoryHash;
            HorizontalLegendLayout: HorizontalLegendLayout;
            Intl: Intl;
            JSON: JSON;
            JSONPRequest: JSONPRequest;
            Lang: Lang;
            LeftAxisLayout: LeftAxisLayout;
            LineSeries: LineSeries;
            Lines: Lines;
            MarkerSeries: MarkerSeries;
            Mock: Mock;
            ModelSync: ModelSync;
            ModelSync_REST: ModelSync_REST;
            Number: Number;
            NumericAxis: NumericAxis;
            Object: Object;
            Parallel: Parallel;
            PieChart: PieChart;
            PieSeries: PieSeries;
            PjaxBase: PjaxBase;
            PjaxContent: PjaxContent;
            Plots: Plots;
            Plugin: Plugin;
            Plugin_Align: Plugin_Align;
            Plugin_AutoComplete: Plugin_AutoComplete;
            Plugin_Base: Plugin_Base;
            Plugin_Cache: Plugin_Cache;
            Plugin_CalendarNavigator: Plugin_CalendarNavigator;
            Plugin_ConsoleFilters: Plugin_ConsoleFilters;
            Plugin_CreateLinkBase: Plugin_CreateLinkBase;
            Plugin_DataTableDataSource: Plugin_DataTableDataSource;
            Plugin_Flick: Plugin_Flick;
            Plugin_Host: Plugin_Host;
            Plugin_NodeFX: Plugin_NodeFX;
            Plugin_Pjax: Plugin_Pjax;
            Plugin_Resize: Plugin_Resize;
            Plugin_ResizeConstrained: Plugin_ResizeConstrained;
            Plugin_ScrollInfo: Plugin_ScrollInfo;
            Plugin_Shim: Plugin_Shim;
            Plugin_WidgetAnim: Plugin_WidgetAnim;
            Pollable: Pollable;
            Profiler: Profiler;
            QueryString: QueryString;
            Record: Record;
            RecordsetFilter: RecordsetFilter;
            RecordsetIndexer: RecordsetIndexer;
            RecordsetSort: RecordsetSort;
            Renderer: Renderer;
            RightAxisLayout: RightAxisLayout;
            SVGCircle: SVGCircle;
            SVGDrawing: SVGDrawing;
            SVGEllipse: SVGEllipse;
            SVGGraphic: SVGGraphic;
            SVGPath: SVGPath;
            SVGPieSlice: SVGPieSlice;
            SVGRect: SVGRect;
            SVGShape: SVGShape;
            Selector: Selector;
            SliderValueRange: SliderValueRange;
            SplineSeries: SplineSeries;
            StackedAreaSeries: StackedAreaSeries;
            StackedAreaSplineSeries: StackedAreaSplineSeries;
            StackedAxis: StackedAxis;
            StackedBarSeries: StackedBarSeries;
            StackedColumnSeries: StackedColumnSeries;
            StackedComboSeries: StackedComboSeries;
            StackedComboSplineSeries: StackedComboSplineSeries;
            StackedLineSeries: StackedLineSeries;
            StackedMarkerSeries: StackedMarkerSeries;
            StackedSplineSeries: StackedSplineSeries;
            StackingUtil: StackingUtil;
            Test: Test;
            Test_ArrayAssert: Test_ArrayAssert;
            Test_Assert: Test_Assert;
            Test_AssertionError: Test_AssertionError;
            Test_ComparisonFailure: Test_ComparisonFailure;
            Test_Console: Test_Console;
            Test_CoverageFormat: Test_CoverageFormat;
            Test_DateAssert: Test_DateAssert;
            Test_EventTarget: Test_EventTarget;
            Test_Mock: Test_Mock;
            Test_Mock_Value: Test_Mock_Value;
            Test_ObjectAssert: Test_ObjectAssert;
            Test_Results: Test_Results;
            Test_Runner: Test_Runner;
            Test_ShouldError: Test_ShouldError;
            Test_ShouldFail: Test_ShouldFail;
            Test_TestCase: Test_TestCase;
            Test_TestFormat: Test_TestFormat;
            Test_TestNode: Test_TestNode;
            Test_TestRunner: Test_TestRunner;
            Test_TestSuite: Test_TestSuite;
            Test_UnexpectedError: Test_UnexpectedError;
            Test_UnexpectedValue: Test_UnexpectedValue;
            Test_Wait: Test_Wait;
            Text: Text;
            Text_AccentFold: Text_AccentFold;
            Text_WordBreak: Text_WordBreak;
            TimeAxis: TimeAxis;
            TopAxisLayout: TopAxisLayout;
            UA: UA;
            Uploader: Uploader;
            VMLCircle: VMLCircle;
            VMLDrawing: VMLDrawing;
            VMLEllipse: VMLEllipse;
            VMLGraphic: VMLGraphic;
            VMLPath: VMLPath;
            VMLPieSlice: VMLPieSlice;
            VMLRect: VMLRect;
            VMLShape: VMLShape;
            ValueChange: ValueChange;
            VerticalLegendLayout: VerticalLegendLayout;
            View_NodeMap: View_NodeMap;
            WidgetAutohide: WidgetAutohide;
            WidgetButtons: WidgetButtons;
            WidgetChild: WidgetChild;
            WidgetModality: WidgetModality;
            WidgetPosition: WidgetPosition;
            WidgetPositionConstrain: WidgetPositionConstrain;
            WidgetStack: WidgetStack;
            WidgetStdMod: WidgetStdMod;
            XML: XML;
            config: config;
            plugin: plugin;
            plugin_NodeFocusManager: plugin_NodeFocusManager;
            plugin_NodeMenuNav: plugin_NodeMenuNav;
            YUI_config: any;
            Global: EventTarget;
            meta: any;
            version: string;
            
            applyTo(id: string, method: string, args: any[]): any;
            applyConfig(o: any): any;
            assert(condition: bool, message: string): any;
            cached(source: Function, cache?: any, refetch?: any): Function;
            bind(f: Function, c: any, args: any): Function;
            bind(f: string, c: any, args: any): Function;
            clone(o: any, safe: bool, f: Function, c: any, owner: any, cloned: any): any[];
            clone(o: any, safe: bool, f: Function, c: any, owner: any, cloned: any): any;
            aggregate(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[]): any;
            augment(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any[]): Function;
            augment(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any): Function;
            augment(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any[]): Function;
            augment(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], args?: any): Function;
            all(selector: string): NodeList;
            after(type: string, fn: Function, context?: any, ...args: any[]): EventHandle;
            destroy(): any;
            error(msg: string, e: Error, src: any): YUI;
            error(msg: string, e: string, src: any): YUI;
            each(o: any, f: Function, c: any, proto: bool): YUI;
            extend(r: Function, s: Function, px: any, sx: any): any;
            dump(o: any, d: number): string;
            delegate(type: string, fn: Function, el: string, filter: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: string, filter: Function, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: Node, filter: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: Node, filter: Function, context: any, args: any): EventHandle;
            instanceOf(o: any, type: any): any;
            guid(pre: string): string;
            namespace(namespace: string): any;
            on(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            once(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            onceAfter(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            message(msg: string, cat: string, src: string, silent: bool): YUI;
            log(msg: string, cat: string, src: string, silent: bool): YUI;
            later(when: number, o: any, fn: Function, data: any, periodic: bool): any;
            later(when: number, o: any, fn: string, data: any, periodic: bool): any;
            mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): Function;
            mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): any;
            mix(receiver: Function, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            mix(receiver: Function, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            mix(receiver: any, supplier: Function, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            mix(receiver: any, supplier: any, overwrite?: bool, whitelist?: String[], mode?: number, merge?: bool): YUI;
            merge(objects: any): any;
            getLocation(): Location;
            one(node: string): Node;
            one(node: HTMLElement): Node;
            one(node: string): any;
            one(node: HTMLElement): any;
            rbind(f: Function, c: any, args: any): Function;
            rbind(f: string, c: any, args: any): Function;
            stamp(o: any, readOnly: bool): string;
            some(o: any, f: Function, c: any, proto: bool): bool;
            soon(fn: Function): any;
            substitute(s: string, o: any, f: Function, recurse: bool): string;
            throttle(fn: Function, ms: number): Function;
            use(modules: string, callback?: (Y: YUI, status: any) => any): YUI;
            use(modules: any[], callback?: (Y: YUI, status: any) => any): YUI;
        }
    
        interface YUIStatic { 
            (o?: any);
            
            GlobalConfig: any;
            
            add(name: string, fn: (Y: YUI, name: string) => any, version: string, details: any): YUI;
            assert(condition: bool, message: string): any;
            fail(message: string): any;
            get(node: string, doc: Node): any;
            get(node: string, doc: HTMLElement): any;
            get(node: HTMLElement, doc: Node): any;
            get(node: HTMLElement, doc: HTMLElement): any;
            io(url: string, config: any): any;
            header(name: string, value: string): any;
            jsonp(url: string, c: Function, args: any): JSONPRequest;
            jsonp(url: string, c: any, args: any): JSONPRequest;
        }
    

    
        interface AnimStatic {
            new (): Anim;
            
            RE_DEFAULT_UNIT: any;
            DEFAULT_UNIT: any;
            behaviors: any;
            DEFAULT_SETTER: any;
            DEFAULT_GETTER: any;
            intervalTime: any;
            
            getBezier(points: any[], t: number): any[];
            pause(): any;
            run(): any;
            stop(): any;
        }
    
        interface AppStatic {
            new (config?: any): App;
            
            Base: App_BaseStatic;
            Transitions: App_Transitions;
            TransitionsNative: App_TransitionsNative;
            Content: App_Content;
            CLASS_NAMES: any;
            serverRouting: bool;
            
        }
    
        interface App_BaseStatic {
            new (config?: any): App_Base;
            
            
        }
    
        interface ArrayListStatic {
            new (items: any[]): ArrayList;
            
            
            addMethod(dest: any, name: string): any;
            addMethod(dest: any, name: String[]): any;
        }
    
        interface ArrayStatic {
            new (thing: any, startIndex?: number, force?: bool): Array;
            
            
            dedupe(array: String[]): any[];
            forEach(): any;
            each(array: any[], fn: (item: any, index: number, array: any[]) => any, thisObj?: any): YUI;
            filter(a: any[], f: Function, o?: any): any[];
            every(a: any[], f: Function, o?: any): bool;
            find(a: any[], f: Function, o?: any): any;
            flatten(a: any[]): any[];
            invoke(items: any[], name: string, ...args: any[]): any[];
            grep(a: any[], pattern: RegExp): any[];
            map(a: any[], f: Function, o?: any): any[];
            lastIndexOf(a: any[], val: any, fromIndex?: number): number;
            numericSort(a: number, b: number): number;
            indexOf(array: any[], value: any, from?: number): number;
            hash(keys: String[], values?: any[]): any;
            partition(a: any[], f: (item: any, index: number, array: any[]) => any, o?: any): any;
            reduce(a: any[], init: any, f: (previousValue: any, currentValue: any, index: number, array: any[]) => any, o?: any): any;
            reject(a: any[], f: Function, o?: any): any[];
            some(array: any[], fn: (value: any, index: number, array: any[]) => any, thisObj?: any): bool;
            test(obj: any): number;
            unique(array: any[], testFn?: (a: any, b: any, index: number, array: any[]) => any): any[];
            zip(a: any[], a2: any[]): any[];
        }
    
        interface AsyncQueueStatic {
            new (callback: Function): AsyncQueue;
            
            defaults: any;
            
        }
    
        interface AttributeLiteStatic {
            new (): AttributeLite;
            
            
        }
    
        interface AutoCompleteListStatic {
            new (config: any): AutoCompleteList;
            
            
        }
    
        interface BaseCoreStatic {
            new (cfg: any): BaseCore;
            
            NAME: string;
            ATTRS: any;
            
        }
    
        interface BaseStatic {
            new (config: any): Base;
            
            NAME: string;
            ATTRS: any;
            
            build(name: Function, main: Function, extensions: Function[], cfg: any): Function;
            create(name: Function, main: Function, extensions: Function[], px: any, sx: any): Function;
            mix(main: Function, extensions: Function[]): Function;
            plug(): any;
            unplug(): any;
        }
    
        interface ButtonCoreStatic {
            new (config: any): ButtonCore;
            
            NAME: string;
            CLASS_NAMES: any;
            
        }
    
        interface ButtonGroupStatic {
            new (config: any): ButtonGroup;
            
            CLASS_NAMES: any;
            
        }
    
        interface ButtonPluginStatic {
            new (config: any): ButtonPlugin;
            
            NAME: string;
            NS: string;
            
        }
    
        interface ButtonStatic {
            new (config: any): Button;
            
            CLASS_NAMES: any;
            
        }
    
        interface CacheOfflineStatic {
            new (): CacheOffline;
            
            NAME: string;
            
            flushAll(): any;
        }
    
        interface CacheStatic {
            new (): Cache;
            
            NAME: string;
            
        }
    
        interface CalendarBaseStatic {
            new (config: any): CalendarBase;
            
            
        }
    
        interface CalendarStatic {
            new (config: any): Calendar;
            
            
        }
    
        interface CircleStatic {
            new (): Circle;
            
            
        }
    
        interface ControllerStatic {
            new (): Controller;
            
            
        }
    
        interface CustomEventStatic {
            new (type: string, o: any): CustomEvent;
            
            keepDeprecatedSubs: bool;
            
        }
    
        interface DD_DDMStatic {
            new (): DD_DDM;
            
            
        }
    
        interface DD_DelegateStatic {
            new (): DD_Delegate;
            
            
        }
    
        interface DD_DragStatic {
            new (): DD_Drag;
            
            START_EVENT: any;
            
        }
    
        interface DD_DropStatic {
            new (): DD_Drop;
            
            
        }
    
        interface DD_ScrollStatic {
            new (): DD_Scroll;
            
            
        }
    
        interface DataSource_FunctionStatic {
            new (): DataSource_Function;
            
            NAME: string;
            
        }
    
        interface DataSource_GetStatic {
            new (): DataSource_Get;
            
            NAME: string;
            
        }
    
        interface DataSource_IOStatic {
            new (): DataSource_IO;
            
            NAME: string;
            
        }
    
        interface DataSource_LocalStatic {
            new (): DataSource_Local;
            
            NAME: string;
            transactions: any;
            
            issueCallback(e: EventFacade, caller: DataSource): any;
        }
    
        interface DialStatic {
            new (config: any): Dial;
            
            
        }
    
        interface Do_AlterArgsStatic {
            new (msg: string, newArgs: any[]): Do_AlterArgs;
            
            
        }
    
        interface Do_AlterReturnStatic {
            new (msg: string, newRetVal: any): Do_AlterReturn;
            
            
        }
    
        interface Do_ErrorStatic {
            new (msg: string, retVal: any): Do_Error;
            
            
        }
    
        interface Do_HaltStatic {
            new (msg: string, retVal: any): Do_Halt;
            
            
        }
    
        interface Do_MethodStatic {
            new (obj: any, sFn: any): Do_Method;
            
            
        }
    
        interface Do_PreventStatic {
            new (msg: string): Do_Prevent;
            
            
        }
    
        interface DrawingStatic {
            new (): Drawing;
            
            
        }
    
        interface EditorBaseStatic {
            new (): EditorBase;
            
            STRINGS: any;
            NAME: any;
            USE: any[];
            NC_KEYS: any;
            TAG2CMD: any;
            TABKEY: any;
            
            FILTER_RGB(String: any): any;
            NORMALIZE_FONTSIZE(): any;
        }
    
        interface EditorSelectionStatic {
            new (): EditorSelection;
            
            CURSOR: any;
            CUR_WRAPID: any;
            DEFAULT_TAG: any;
            TMP: any;
            BLOCKS: any;
            ALL: any;
            REG_NOHTML: any;
            REG_NON: any;
            REG_CHAR: any;
            REG_FONTFAMILY: any;
            
            cleanCursor(): any;
            filter(): any;
            filterBlocks(): any;
            getText(node: Node): string;
            removeFontFamily(): any;
            resolve(n: HTMLElement): Node;
            unfilter(): string;
        }
    
        interface EllipseStatic {
            new (): Ellipse;
            
            
        }
    
        interface EventHandleStatic {
            new (evt: CustomEvent, sub: Subscriber): EventHandle;
            
            
        }
    
        interface FileFlashStatic {
            new (config: any): FileFlash;
            
            
        }
    
        interface FileHTML5Static {
            new (config: any): FileHTML5;
            
            
            canUpload(): any;
            isValidFile(file: File): any;
        }
    
        interface FrameStatic {
            new (): Frame;
            
            NAME: string;
            META: string;
            DOC_TYPE: string;
            PAGE_HTML: string;
            HTML: string;
            DEFAULT_CSS: string;
            DOM_EVENTS: any;
            THROTTLE_TIME: number;
            
            getDocType(): string;
        }
    
        interface Get_TransactionStatic {
            new (): Get_Transaction;
            
            
        }
    
        interface GraphicBaseStatic {
            new (cfg: any): GraphicBase;
            
            
        }
    
        interface GraphicStatic {
            new (): Graphic;
            
            
        }
    
        interface HistoryHTML5Static {
            new (config: any): HistoryHTML5;
            
            SRC_POPSTATE: string;
            
        }
    
        interface IOStatic {
            new (config: any): IO;
            
            delay: number;
            
            promote(): any;
            queue(): any;
            request(): any;
        }
    
        interface ImgLoadGroupStatic {
            new (): ImgLoadGroup;
            
            
        }
    
        interface ImgLoadImgObjStatic {
            new (): ImgLoadImgObj;
            
            
        }
    
        interface LazyModelListStatic {
            new (): LazyModelList;
            
            
        }
    
        interface LoaderStatic {
            new (config: any): Loader;
            
            
        }
    
        interface MatrixStatic {
            new (): Matrix;
            
            
        }
    
        interface ModelListStatic {
            new (config: any): ModelList;
            
            
        }
    
        interface ModelStatic {
            new (): Model;
            
            
        }
    
        interface NodeListStatic {
            new (nodes: string): NodeList;
            
            
            getDOMNodes(nodelist: NodeList): any[];
        }
    
        interface NodeStatic {
            new (node: HTMLElement): Node;
            
            DOM_EVENTS: any;
            NAME: string;
            ATTRS: any;
            
            create(html: string, doc: HTMLDocument): Node;
            create(html: string ): Node;
            DEFAULT_GETTER(name: string): any;
            DEFAULT_SETTER(name: string, val: any): any;
            addMethod(name: string, fn: Function, context: any): any;
            plug(plugin: Function, config: any): any;
            plug(plugin: any[], config: any): any;
            one(node: string): Node;
            one(node: HTMLElement): Node;
            one(node: string): any;
            one(node: HTMLElement): any;
            importMethod(host: any, name: string, altName: string, context: any): any;
            getDOMNode(node: Node): HTMLElement;
            getDOMNode(node: HTMLElement): HTMLElement;
            scrubVal(node: any): Node;
            scrubVal(node: any): NodeList;
            scrubVal(node: any): any;
            unplug(plugin: Function): any;
            unplug(plugin: any[]): any;
        }
    
        interface OverlayStatic {
            new (object: any): Overlay;
            
            
        }
    
        interface PanelStatic {
            new (): Panel;
            
            
        }
    
        interface PathStatic {
            new (): Path;
            
            
        }
    
        interface PjaxStatic {
            new (config?: any): Pjax;
            
            defaultRoute: any[];
            
        }
    
        interface Plugin_DDConstrainedStatic {
            new (): Plugin_DDConstrained;
            
            
        }
    
        interface Plugin_DDNodeScrollStatic {
            new (): Plugin_DDNodeScroll;
            
            
        }
    
        interface Plugin_DDProxyStatic {
            new (): Plugin_DDProxy;
            
            
        }
    
        interface Plugin_DDWindowScrollStatic {
            new (): Plugin_DDWindowScroll;
            
            
        }
    
        interface Plugin_DragStatic {
            new (): Plugin_Drag;
            
            
        }
    
        interface Plugin_DropStatic {
            new (): Plugin_Drop;
            
            
        }
    
        interface Plugin_EditorBRStatic {
            new (): Plugin_EditorBR;
            
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_EditorBidiStatic {
            new (): Plugin_EditorBidi;
            
            RE_TEXT_ALIGN: any;
            NS: any;
            NAME: any;
            _NODE_SELECTED: any;
            DIV_WRAPPER: any;
            BLOCKS: any;
            EVENTS: any;
            
            addParents(): any;
            blockParent(): any;
            removeTextAlign(): any;
        }
    
        interface Plugin_EditorListsStatic {
            new (): Plugin_EditorLists;
            
            NS: any;
            NAME: any;
            REMOVE: any;
            NONSEL: any;
            
        }
    
        interface Plugin_EditorParaBaseStatic {
            new (): Plugin_EditorParaBase;
            
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_EditorParaIEStatic {
            new (): Plugin_EditorParaIE;
            
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_EditorParaStatic {
            new (): Plugin_EditorPara;
            
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_EditorTabStatic {
            new (): Plugin_EditorTab;
            
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_ExecCommandStatic {
            new (): Plugin_ExecCommand;
            
            COMMANDS: any;
            NS: any;
            NAME: any;
            
        }
    
        interface Plugin_ResizeProxyStatic {
            new (): Plugin_ResizeProxy;
            
            
        }
    
        interface Plugin_ScrollViewListStatic {
            new (): Plugin_ScrollViewList;
            
            ATTRS: any;
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_ScrollViewPaginatorStatic {
            new (): Plugin_ScrollViewPaginator;
            
            SNAP_TO_CURRENT: any;
            ATTRS: any;
            NS: string;
            
        }
    
        interface Plugin_ScrollViewScrollbarsStatic {
            new (): Plugin_ScrollViewScrollbars;
            
            ATTRS: any;
            SCROLLBAR_TEMPLATE: any;
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_SortScrollStatic {
            new (): Plugin_SortScroll;
            
            
        }
    
        interface QueueStatic {
            new (item: any): Queue;
            
            
        }
    
        interface RecordsetStatic {
            new (config: any): Recordset;
            
            
        }
    
        interface RectStatic {
            new (): Rect;
            
            
        }
    
        interface ResizeStatic {
            new (config: any): Resize;
            
            ATTRS: any;
            NAME: string;
            
        }
    
        interface RouterStatic {
            new (config?: any): Router;
            
            
        }
    
        interface SWFDetectStatic {
            new (): SWFDetect;
            
            
        }
    
        interface SWFStatic {
            new (id: string, swfURL: string, p_oAttributes: any): SWF;
            
            
        }
    
        interface ScrollViewStatic {
            new (config: any): ScrollView;
            
            SNAP_DURATION: number;
            SNAP_EASING: string;
            EASING: string;
            FRAME_STEP: number;
            BOUNCE_RANGE: number;
            UI_SRC: string;
            CLASS_NAMES: any;
            
        }
    
        interface ShapeGroupStatic {
            new (): ShapeGroup;
            
            
        }
    
        interface ShapeStatic {
            new (cfg: any): Shape;
            
            
        }
    
        interface SliderBaseStatic {
            new (config: any): SliderBase;
            
            
        }
    
        interface SliderStatic {
            new (config: any): Slider;
            
            
        }
    
        interface SortableStatic {
            new (): Sortable;
            
            
            _test(node: Node, test: string): any;
            _test(node: Node, test: Node): any;
            getSortable(node: string): any;
            getSortable(node: Node): any;
            reg(Sortable: any, String: any): any;
            unreg(Sortable: any, String: any): any;
        }
    
        interface StateStatic {
            new (): State;
            
            
        }
    
        interface StyleSheetStatic {
            new (seed: string, name: string): StyleSheet;
            
            
            isValidSelector(sel: string): bool;
            register(name: string, sheet: StyleSheet): bool;
            toCssText(css: any, cssText: string): string;
        }
    
        interface SubscriberStatic {
            new (fn: Function, context: any, args: any[]): Subscriber;
            
            
        }
    
        interface SynthRegistryStatic {
            new (el: HTMLElement, yuid: string, key: string): SynthRegistry;
            
            
        }
    
        interface SyntheticEventStatic {
            new (cfg: any): SyntheticEvent;
            
            Notifier: SyntheticEvent_NotifierStatic;
            
        }
    
        interface SyntheticEvent_NotifierStatic {
            new (handle: EventHandle, emitFacade: bool): SyntheticEvent_Notifier;
            
            
        }
    
        interface TabStatic {
            new (config: any): Tab;
            
            
        }
    
        interface TabViewStatic {
            new (config: any): TabView;
            
            
        }
    
        interface Test_ReporterStatic {
            new (url: string, format: Function): Test_Reporter;
            
            
        }
    
        interface ToggleButtonStatic {
            new (config: any): ToggleButton;
            
            CLASS_NAMES: any;
            
        }
    
        interface TransitionStatic {
            new (): Transition;
            
            
        }
    
        interface UploaderFlashStatic {
            new (config: any): UploaderFlash;
            
            TYPE: string;
            SELECT_FILES_BUTTON: string;
            FLASH_CONTAINER: string;
            
        }
    
        interface UploaderHTML5Static {
            new (): UploaderHTML5;
            
            TYPE: string;
            SELECT_FILES_BUTTON: string;
            HTML5FILEFIELD_TEMPLATE: string;
            
        }
    
        interface Uploader_QueueStatic {
            new (config: any): Uploader_Queue;
            
            UPLOADING: string;
            STOPPED: string;
            RESTART_AFTER: string;
            RESTART_ASAP: string;
            STOP: string;
            CONTINUE: string;
            
        }
    
        interface ViewStatic {
            new (): View;
            
            NodeMap: View_NodeMap;
            
        }
    
        interface WidgetParentStatic {
            new (config: any): WidgetParent;
            
            
        }
    
        interface WidgetPositionAlignStatic {
            new (config: any): WidgetPositionAlign;
            
            CC: string;
            LC: string;
            BC: string;
            RC: string;
            TC: string;
            BR: string;
            BL: string;
            TR: string;
            TL: string;
            
        }
    
        interface WidgetStatic {
            new (config: any): Widget;
            
            HTML_PARSER: any;
            ATTRS: any;
            UI_SRC: string;
            NAME: string;
            
            getByNode(node: Node): Widget;
            getByNode(node: string): Widget;
        }
    
        interface YQLRequestStatic {
            new (sql: string, callback: Function, params: any, opts: any): YQLRequest;
            
            ENV: any;
            BASE_URL: any;
            PROTO: any;
            FORMAT: any;
            
        }
    
        interface YQLStatic {
            new (sql: string, callback: Function, params: any, opts: any): YQL;
            
            
        }
    

    
        interface App_Content {
            
            route: any[];
            
            showContent(content: HTMLElement, options?: any, callback?: (view: View) => any): any;
            showContent(content: Node, options?: any, callback?: (view: View) => any): any;
            showContent(content: string, options?: any, callback?: (view: View) => any): any;
        }
    
        interface App_Transitions {
            
            FX: any;
            transitions: any;
            
            showView(view: string, config?: any, options?: any, callback?: (view: View) => any): App_Transitions;
            showView(view: View, config?: any, options?: any, callback?: (view: View) => any): App_Transitions;
        }
    
        interface App_TransitionsNative {
            
            
        }
    
        interface AreaSeries {
            
            
        }
    
        interface AreaSplineSeries {
            
            
        }
    
        interface ArraySort {
            
            
            compare(a: any, b: any, desc: bool): bool;
        }
    
        interface Attribute {
            
            INVALID_VALUE: any;
            
        }
    
        interface AttributeCore {
            
            INVALID_VALUE: any;
            
            addAttr(name: string, config: any, lazy: bool): any;
            attrAdded(name: string): bool;
            addAttrs(cfgs: any, values: any, lazy: bool): any;
            get(name: string): any;
            getAttrs(attrs: any[]): any;
            getAttrs(attrs: bool): any;
            set(name: string, value: any): any;
            setAttrs(attrs: any): any;
        }
    
        interface AttributeEvents {
            
            
            set(name: string, value: any, opts: any): any;
            setAttrs(attrs: any, opts: any): any;
        }
    
        interface AttributeExtras {
            
            
            modifyAttr(name: string, config: any): any;
            removeAttr(name: string): any;
            reset(name: string): any;
        }
    
        interface AutoComplete {
            
            
        }
    
        interface AutoCompleteBase {
            
            SOURCE_TYPES: any;
            
            clearCache(): AutoCompleteBase;
            sendRequest(query?: string, requestTemplate?: Function): AutoCompleteBase;
        }
    
        interface AutoCompleteFilters {
            
            
            charMatchFold(query: string, results: any[]): any[];
            charMatch(query: string, results: any[]): any[];
            charMatchCase(query: string, results: any[]): any[];
            phraseMatchFold(query: string, results: any[]): any[];
            phraseMatch(query: string, results: any[]): any[];
            phraseMatchCase(query: string, results: any[]): any[];
            startsWithFold(query: string, results: any[]): any[];
            subWordMatchFold(query: string, results: any[]): any[];
            startsWith(query: string, results: any[]): any[];
            startsWithCase(query: string, results: any[]): any[];
            subWordMatch(query: string, results: any[]): any[];
            subWordMatchCase(query: string, results: any[]): any[];
            wordMatch(query: string, results: any[]): any[];
            wordMatchFold(query: string, results: any[]): any[];
            wordMatchCase(query: string, results: any[]): any[];
        }
    
        interface AutoCompleteHighlighters {
            
            
            charMatchFold(query: string, results: any[]): any[];
            charMatch(query: string, results: any[]): any[];
            charMatchCase(query: string, results: any[]): any[];
            phraseMatchFold(query: string, results: any[]): any[];
            phraseMatch(query: string, results: any[]): any[];
            phraseMatchCase(query: string, results: any[]): any[];
            startsWithFold(query: string, results: any[]): any[];
            subWordMatchFold(query: string, results: any[]): any[];
            subWordMatchCase(query: string, results: any[]): any[];
            subWordMatch(query: string, results: any[]): any[];
            startsWithCase(query: string, results: any[]): any[];
            startsWith(query: string, results: any[]): any[];
            wordMatch(query: string, results: any[]): any[];
            wordMatchCase(query: string, results: any[]): any[];
            wordMatchFold(query: string, results: any[]): any[];
        }
    
        interface Axis {
            
            
            getMaxLabelBounds(): any;
            getMinLabelBounds(): any;
        }
    
        interface AxisType {
            
            
            addKey(value: any): any;
            getKeyValueAt(key: string, index: number): any;
            getDataByKey(value: string): any;
            getTotalMajorUnits(): any;
            getMajorUnitDistance(len: number, uiLen: number, majorUnit: any): any;
            getEdgeOffset(ct: number, l: number): any;
            getLabelByIndex(i: number, l: number): any;
            getMinimumValue(): any;
            getMaximumValue(): any;
            removeKey(value: string): any;
        }
    
        interface BarSeries {
            
            
        }
    
        interface BottomAxisLayout {
            
            
        }
    
        interface CanvasCircle {
            
            
        }
    
        interface CanvasDrawing {
            
            
            clear(): any;
            closePath(): any;
            curveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            end(): any;
            drawRect(x: number, y: number, w: number, h: number): any;
            lineTo(point1: number, point2: number): any;
            moveTo(x: number, y: number): any;
            relativeMoveTo(x: number, y: number): any;
            quadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
            relativeQuadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
        }
    
        interface CanvasEllipse {
            
            
        }
    
        interface CanvasGraphic {
            
            
            batch(method: Function): any;
            clear(): any;
            addShape(cfg: any): any;
            destroy(): any;
            getXY(): any;
            getShapeById(id: string): any;
            removeShape(shape: Shape): any;
            removeShape(shape: string): any;
            removeAllShapes(): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
        }
    
        interface CanvasPath {
            
            
            end(): any;
        }
    
        interface CanvasPieSlice {
            
            
        }
    
        interface CanvasRect {
            
            
        }
    
        interface CanvasShape {
            
            
            compareTo(refNode: HTMLElement): bool;
            compareTo(refNode: Node): bool;
            contains(needle: CanvasShape): any;
            contains(needle: HTMLElement): any;
            addClass(className: string): any;
            destroy(): any;
            getXY(): any;
            getBounds(): any;
            removeClass(className: string): any;
            rotate(deg: number): any;
            scale(val: number): any;
            setXY(Contains: any[]): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
            skew(x: number, y: number): any;
            skewX(x: number): any;
            skewY(y: number): any;
            toFront(): any;
            translateY(y: number): any;
            translateX(x: number): any;
            translate(x: number, y: number): any;
            test(selector: string): any;
        }
    
        interface CartesianChart {
            
            
            _addToAxesCollection(position: string, axis: Axis): any;
            _getAriaMessage(key: number): any;
            getSeriesItems(series: CartesianSeries, index: number): any;
        }
    
        interface CartesianSeries {
            
            
            getTotalValues(): any;
        }
    
        interface CategoryAxis {
            
            
            formatLabel(value: any, format: any): any;
            getMaximumValue(): any;
            getMinimumValue(): any;
            getLabelByIndex(i: number, l: number): any;
            getKeyValueAt(key: string, index: number): any;
            getEdgeOffset(ct: number, l: number): any;
            getMajorUnitDistance(len: number, uiLen: number, majorUnit: any): any;
            getTotalMajorUnits(majorUnit: any, len: number): any;
            getDataByKey(value: string): any;
        }
    
        interface Chart {
            
            
        }
    
        interface ChartBase {
            
            
            _getAllKeys(dp: any[]): any;
            getAxisByKey(val: string): any;
            hideTooltip(): any;
            getCategoryAxis(): any;
            getSeries(val: any): any;
            toggleTooltip(e: any): any;
        }
    
        interface ChartLegend {
            
            
        }
    
        interface CircleGroup {
            
            
            drawShape(cfg: any): any;
        }
    
        interface ClassNameManager {
            
            classNamePrefix: string;
            classNameDelimiter: string;
            
            getClassName(args: string, skipPrefix: bool): any;
        }
    
        interface ClickableRail {
            
            
        }
    
        interface ColumnSeries {
            
            
        }
    
        interface ComboSeries {
            
            
        }
    
        interface ComboSplineSeries {
            
            
        }
    
        interface Console {
            
            NAME: string;
            LOG_LEVEL_INFO: string;
            LOG_LEVEL_WARN: string;
            LOG_LEVEL_ERROR: string;
            ENTRY_CLASSES: any;
            CHROME_CLASSES: any;
            HEADER_TEMPLATE: string;
            BODY_TEMPLATE: string;
            FOOTER_TEMPLATE: string;
            ENTRY_TEMPLATE: string;
            ATTRS: any;
            
            clearConsole(): Console;
            collapse(): Console;
            expand(): Console;
            log(arg: any): Console;
            reset(): Console;
            printBuffer(limit: number): Console;
            scrollToLatest(): Console;
            syncUI(): any;
        }
    
        interface Cookie {
            
            
            get(name: string, options: Function): any;
            get(name: string, options: any): any;
            exists(name: string): bool;
            getSub(name: string, subName: string, converter: Function): any;
            getSubs(name: string): any;
            removeSub(name: string, subName: string, options: any): string;
            remove(name: string, options: any): string;
            setSubs(name: string, value: any, options: any): string;
            setSub(name: string, subName: string, value: any, options: any): string;
            set(name: string, value: any, options: any): string;
        }
    
        interface CurveUtil {
            
            
        }
    
        interface DD {
            
            Scroll: DD_ScrollStatic;
            Drop: DD_DropStatic;
            Drag: DD_DragStatic;
            Delegate: DD_DelegateStatic;
            DDM: DD_DDMStatic;
            
        }
    
        interface DOM {
            
            
            addHTML(node: HTMLElement, content: HTMLElement, where: HTMLElement): any;
            addHTML(node: HTMLElement, content: any[], where: HTMLElement): any;
            addHTML(node: HTMLElement, content: HTMLCollection, where: HTMLElement): any;
            create(html: string, doc: HTMLDocument): HTMLElement;
            create(html: string, doc: HTMLDocument): DocumentFragment;
            contains(element: HTMLElement, needle: HTMLElement): bool;
            byId(id: string, doc: any): HTMLElement;
            byId(id: string, doc: any): any;
            addClass(element: HTMLElement, className: string): any;
            getAttribute(el: HTMLElement, attr: string): string;
            elementByAxis(element: HTMLElement, axis: string, fn: Function, all: bool): HTMLElement;
            elementByAxis(element: HTMLElement, axis: string, fn: Function, all: bool): any;
            docHeight(): number;
            docWidth(): number;
            docScrollX(): number;
            docScrollY(): number;
            getText(element: HTMLElement): string;
            hasClass(element: HTMLElement, className: string): bool;
            inDoc(element: HTMLElement, doc: HTMLElement): bool;
            intersect(element: HTMLElement, element2: HTMLElement, altRegion: any): any;
            intersect(element: HTMLElement, element2: any, altRegion: any): any;
            inRegion(node: any, node2: any, all: bool, altRegion: any): bool;
            inViewportRegion(element: HTMLElement, all: bool, altRegion: any): bool;
            getXY(element: any): any[];
            getScrollbarWidth(): number;
            getX(element: any): number;
            getY(element: any): number;
            getStyle(An: HTMLElement, att: string): any;
            getComputedStyle(An: HTMLElement, att: string): string;
            region(element: HTMLElement): any;
            removeClass(element: HTMLElement, className: string): any;
            replaceClass(element: HTMLElement, oldClassName: string, newClassName: string): any;
            setAttribute(el: HTMLElement, attr: string, val: string): any;
            setText(element: HTMLElement, content: string): any;
            setStyles(node: HTMLElement, hash: any): any;
            setStyle(An: HTMLElement, att: string, val: string): any;
            setStyle(An: HTMLElement, att: string, val: number): any;
            setHeight(element: HTMLElement, size: string): any;
            setHeight(element: HTMLElement, size: number): any;
            setWidth(element: HTMLElement, size: string): any;
            setWidth(element: HTMLElement, size: number): any;
            setY(element: any, y: number): any;
            setXY(element: any, xy: any[], noRetry: bool): any;
            setX(element: any, x: number): any;
            swapXY(node: Node, otherNode: Node): Node;
            toggleClass(element: HTMLElement, className: string, addClass: bool): any;
            viewportRegion(): any;
            winWidth(): number;
            winHeight(): number;
        }
    
        interface DOMEventFacade {
            
            currentTarget: Node;
            button: number;
            charCode: number;
            clientY: number;
            clientX: number;
            ctrlKey: bool;
            altKey: bool;
            _GESTURE_MAP: any;
            changedTouches: DOMEventFacade[];
            metaKey: bool;
            pageX: number;
            pageY: number;
            keyCode: number;
            relatedTarget: Node;
            shiftKey: bool;
            touches: DOMEventFacade[];
            targetTouches: DOMEventFacade[];
            type: string;
            target: Node;
            wheelDelta: number;
            which: number;
            
            halt(immediate: bool): any;
            preventDefault(returnValue: string): any;
            stopPropagation(): any;
            stopImmediatePropagation(): any;
        }
    
        interface DataSchema {
            
            XML: DataSchema_XML;
            Text: DataSchema_Text;
            JSON: DataSchema_JSON;
            Base: DataSchema_Base;
            Array: DataSchema_Array;
            
        }
    
        interface DataSchema_Array {
            
            
            apply(schema?: any, data?: any[]): any;
        }
    
        interface DataSchema_Base {
            
            
            apply(schema: any, data: any): any;
            parse(value: any, field: any): any;
        }
    
        interface DataSchema_JSON {
            
            
            apply(schema?: any, data?: any): any;
            apply(schema?: any, data?: any[]): any;
            apply(schema?: any, data?: string): any;
            getPath(locator: string): String[];
            getLocationValue(path: String[], data: string): any;
        }
    
        interface DataSchema_Text {
            
            
            apply(schema: any, data: string): any;
        }
    
        interface DataSchema_XML {
            
            
            apply(schema: any, data: any): any;
        }
    
        interface DataSource {
            
            Local: DataSource_LocalStatic;
            IO: DataSource_IOStatic;
            Get: DataSource_GetStatic;
            Function: DataSource_FunctionStatic;
            
        }
    
        interface DataSourceArraySchema {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataSourceCache {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataSourceCacheExtension {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataSourceJSONSchema {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataSourceTextSchema {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataSourceXMLSchema {
            
            NS: string;
            NAME: string;
            
        }
    
        interface DataTable {
            
            TableView: DataTable_TableView;
            Sortable: DataTable_Sortable;
            Scrollable: DataTable_Scrollable;
            Mutable: DataTable_Mutable;
            Message: DataTable_Message;
            HeaderView: DataTable_HeaderView;
            Core: DataTable_Core;
            ColumnWidths: DataTable_ColumnWidths;
            BodyView: DataTable_BodyView;
            Base: DataTable_Base;
            COL_TEMPLATE: string;
            COLGROUP_TEMPLATE: string;
            data: ModelList;
            MESSAGE_TEMPLATE: string;
            _messageNode: Node;
            SORTABLE_HEADER_TEMPLATE: string;
            
            _getRecordType(val: Model): Model;
            addColumn(config: any, index?: number): DataTable;
            addColumn(config: any, index?: Number[]): DataTable;
            addColumn(config: string, index?: number): DataTable;
            addColumn(config: string, index?: Number[]): DataTable;
            addRow(data: any, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            addRows(data: Object[], config?: any, callback?: (err: Error, response: any) => any): DataTable;
            modifyRow(id: any, data: any, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            modifyRow(id: string, data: any, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            modifyRow(id: number, data: any, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            moveColumn(name: string, index: number): DataTable;
            moveColumn(name: string, index: Number[]): DataTable;
            moveColumn(name: number, index: number): DataTable;
            moveColumn(name: number, index: Number[]): DataTable;
            moveColumn(name: Number[], index: number): DataTable;
            moveColumn(name: Number[], index: Number[]): DataTable;
            moveColumn(name: any, index: number): DataTable;
            moveColumn(name: any, index: Number[]): DataTable;
            modifyColumn(name: string, config: any): DataTable;
            modifyColumn(name: number, config: any): DataTable;
            modifyColumn(name: Number[], config: any): DataTable;
            modifyColumn(name: any, config: any): DataTable;
            hideMessage(): DataTable;
            getRecord(seed: number): Model;
            getRecord(seed: string): Model;
            getRecord(seed: Node): Model;
            getColumn(name: string): any;
            getColumn(name: number): any;
            getColumn(name: Number[]): any;
            scrollTo(id: string): DataTable;
            scrollTo(id: number): DataTable;
            scrollTo(id: Number[]): DataTable;
            scrollTo(id: Node): DataTable;
            removeRow(id: any, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            removeRow(id: string, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            removeRow(id: number, config?: any, callback?: (err: Error, response: any) => any): DataTable;
            removeColumn(name: string): DataTable;
            removeColumn(name: number): DataTable;
            removeColumn(name: Number[]): DataTable;
            removeColumn(name: any): DataTable;
            sort(fields: string, payload?: any): DataTable;
            sort(fields: String[], payload?: any): DataTable;
            sort(fields: any, payload?: any): DataTable;
            sort(fields: Object[], payload?: any): DataTable;
            setColumnWidth(id: number, width: number): DataTable;
            setColumnWidth(id: number, width: string): DataTable;
            setColumnWidth(id: string, width: number): DataTable;
            setColumnWidth(id: string, width: string): DataTable;
            setColumnWidth(id: any, width: number): DataTable;
            setColumnWidth(id: any, width: string): DataTable;
            showMessage(message: string): DataTable;
            toggleSort(fields: string, payload?: any): DataTable;
            toggleSort(fields: String[], payload?: any): DataTable;
        }
    
        interface DataTable_Base {
            
            _displayColumns: Object[];
            
            delegate(type: string, fn: Function, spec: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, spec: Function, context: any, args: any): EventHandle;
            getRow(id: number): Node;
            getRow(id: string): Node;
            getRow(id: Model): Node;
            getCell(seed: Number[], shift?: Number[]): Node;
            getCell(seed: Number[], shift?: string): Node;
            getCell(seed: Node, shift?: Number[]): Node;
            getCell(seed: Node, shift?: string): Node;
            syncUI(): any;
        }
    
        interface DataTable_BodyView {
            
            CELL_TEMPLATE: string;
            CLASS_EVEN: string;
            CLASS_ODD: string;
            ROW_TEMPLATE: string;
            TBODY_TEMPLATE: string;
            host: any;
            
            getRow(id: number): Node;
            getRow(id: string): Node;
            getRow(id: Model): Node;
            getRecord(seed: string): Model;
            getRecord(seed: Node): Model;
            getCell(seed: Number[], shift?: Number[]): Node;
            getCell(seed: Number[], shift?: string): Node;
            getCell(seed: Node, shift?: Number[]): Node;
            getCell(seed: Node, shift?: string): Node;
            render(): View;
        }
    
        interface DataTable_ColumnWidths {
            
            
        }
    
        interface DataTable_Core {
            
            
        }
    
        interface DataTable_HeaderView {
            
            CELL_TEMPLATE: string;
            columns: any[];
            ROW_TEMPLATE: string;
            THEAD_TEMPLATE: string;
            source: any;
            
            render(): View;
        }
    
        interface DataTable_Message {
            
            
        }
    
        interface DataTable_Mutable {
            
            
        }
    
        interface DataTable_Scrollable {
            
            
        }
    
        interface DataTable_Sortable {
            
            
        }
    
        interface DataTable_TableView {
            
            CAPTION_TEMPLATE: string;
            TABLE_TEMPLATE: string;
            body: any;
            displayColumns: Object[];
            foot: any;
            head: any;
            
            getRow(id: number): Node;
            getRow(id: string): Node;
            getRow(id: Model): Node;
            getRecord(seed: string): Model;
            getRecord(seed: Node): Model;
            getCell(seed: Number[], shift?: Number[]): Node;
            getCell(seed: Number[], shift?: string): Node;
            getCell(seed: Node, shift?: Number[]): Node;
            getCell(seed: Node, shift?: string): Node;
            render(): View;
        }
    
        interface Date {
            
            
            areEqual(aDate: Date, bDate: Date): bool;
            addDays(oDate: Date, numMonths: number): Date;
            addMonths(oDate: Date, numMonths: number): Date;
            addYears(oDate: Date, numYears: number): Date;
            daysInMonth(oDate: Date): number;
            format(oDate: Date, oConfig: any): string;
            parse(data: string): Date;
            parse(data: number): Date;
            listOfDatesInMonth(oDate: Date): any[];
            isInRange(aDate: Date, bDate: Date, cDate: Date): bool;
            isGreaterOrEqual(aDate: Date, bDate: Date): bool;
            isGreater(aDate: Date, bDate: Date): bool;
            isValidDate(oDate: Date): bool;
        }
    
        interface Do {
            
            AlterArgs: Do_AlterArgsStatic;
            AlterReturn: Do_AlterReturnStatic;
            Halt: Do_HaltStatic;
            Prevent: Do_PreventStatic;
            Error: Do_ErrorStatic;
            Method: Do_MethodStatic;
            currentRetVal: any;
            objs: any;
            originalRetVal: any;
            
            after(fn: Function, obj: any, sFn: string, c: any, arg: any): string;
            before(fn: Function, obj: any, sFn: string, c: any, arg: any): string;
            detach(handle: string): any;
        }
    
        interface Easing {
            
            
            backIn(t: number, b: number, c: number, d: number, s: number): number;
            backOut(t: number, b: number, c: number, d: number, s: number): number;
            backBoth(t: number, b: number, c: number, d: number, s: number): number;
            bounceIn(t: number, b: number, c: number, d: number): number;
            bounceOut(t: number, b: number, c: number, d: number): number;
            bounceBoth(t: number, b: number, c: number, d: number): number;
            easeIn(t: number, b: number, c: number, d: number): number;
            easeOut(t: number, b: number, c: number, d: number): number;
            easeBoth(t: number, b: number, c: number, d: number): number;
            easeInStrong(t: number, b: number, c: number, d: number): number;
            easeOutStrong(t: number, b: number, c: number, d: number): number;
            easeBothStrong(t: number, b: number, c: number, d: number): number;
            elasticIn(t: number, b: number, c: number, d: number, a: number, p: number): number;
            elasticOut(t: number, b: number, c: number, d: number, a: number, p: number): number;
            elasticBoth(t: number, b: number, c: number, d: number, a: number, p: number): number;
            easeNone(t: number, b: number, c: number, d: number): number;
        }
    
        interface EllipseGroup {
            
            
        }
    
        interface Escape {
            
            
            html(string: string): string;
            regex(string: string): string;
        }
    
        interface Event {
            
            DOMReady: bool;
            POLL_INTERVAL: number;
            POLL_RETRYS: number;
            lastError: Error;
            
            define(type: string, config: any, force: bool): SyntheticEvent;
            defineOutside(event: string, name: string): any;
            attach(type: string, fn: Function, el: string, context: any, args: bool): EventHandle;
            attach(type: string, fn: Function, el: string, context: any, args: any): EventHandle;
            attach(type: string, fn: Function, el: HTMLElement, context: any, args: bool): EventHandle;
            attach(type: string, fn: Function, el: HTMLElement, context: any, args: any): EventHandle;
            attach(type: string, fn: Function, el: any[], context: any, args: bool): EventHandle;
            attach(type: string, fn: Function, el: any[], context: any, args: any): EventHandle;
            attach(type: string, fn: Function, el: NodeList, context: any, args: bool): EventHandle;
            attach(type: string, fn: Function, el: NodeList, context: any, args: any): EventHandle;
            compileFilter(selector: string): Function;
            flick(cb: Function, point: any[], axis: string, distance: number, duration: number): any;
            detachDelegate(node: Node, subscription: any[], notifier: bool): any;
            generateId(el: any): string;
            detach(type: string, fn: Function, el: string): bool;
            detach(type: string, fn: Function, el: HTMLElement): bool;
            detach(type: string, fn: Function, el: any[]): bool;
            detach(type: string, fn: Function, el: NodeList): bool;
            detach(type: string, fn: Function, el: EventHandle): bool;
            delegate(type: string, fn: Function, el: string, filter: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: string, filter: Function, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: Node, filter: string, context: any, args: any): EventHandle;
            delegate(type: string, fn: Function, el: Node, filter: Function, context: any, args: any): EventHandle;
            onAvailable(id: string, fn: Function, p_obj: any, p_override: bool, checkContent: bool): any;
            onAvailable(id: string, fn: Function, p_obj: any, p_override: any, checkContent: bool): any;
            onAvailable(id: string[], fn: Function, p_obj: any, p_override: bool, checkContent: bool): any;
            onAvailable(id: string[], fn: Function, p_obj: any, p_override: any, checkContent: bool): any;
            onContentReady(id: string, fn: Function, obj: any, override: bool): any;
            onContentReady(id: string, fn: Function, obj: any, override: any): any;
            getEvent(e: Event, el: HTMLElement): Event;
            getListeners(el: HTMLElement, type: string): CustomEvent;
            getListeners(el: string, type: string): CustomEvent;
            on(node: Node, subscription: any[], notifier: bool): any;
            move(cb: Function, path: any, duration: number): any;
            pinch(cb: Function, center: any[], startRadius: number, endRadius: number, duration: number, start: number, rotation: number): any;
            purgeElement(el: HTMLElement, recurse: bool, type: string): any;
            rotate(cb: Function, center: any[], startRadius: number, endRadius: number, duration: number, start: number, rotation: number): any;
            simulate(target: HTMLElement, type: string, options: any): undefined;
            simulateGesture(node: HTMLElement, name: string, options?: any, cb?: (err: Error) => any): undefined;
            simulateGesture(node: Node, name: string, options?: any, cb?: (err: Error) => any): undefined;
        }
    
        interface EventFacade {
            
            currentTarget: Node;
            details: any[];
            relatedTarget: Node;
            type: string;
            target: Node;
            
            halt(immediate: bool): any;
            preventDefault(): any;
            stopPropagation(): any;
            stopImmediatePropagation(): any;
        }
    
        interface EventTarget {
            
            before(): any;
            after(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            bubble(evt: CustomEvent): bool;
            addTarget(o: EventTarget): any;
            fire(type: string, ...arguments: Object[]): EventTarget;
            fire(type: any, ...arguments: Object[]): EventTarget;
            detachAll(type: string): any;
            detach(type: string, fn: Function, context: any): EventTarget;
            detach(type: any, fn: Function, context: any): EventTarget;
            getTargets(): any;
            once(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            onceAfter(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            on(type: string, fn: Function, context?: any, ...arg: any[]): EventHandle;
            getEvent(type: string, prefixed: string): CustomEvent;
            parseType(type: string, pre?: string): any[];
            removeTarget(o: EventTarget): any;
            publish(type: string, opts: any): CustomEvent;
            subscribe(): any;
            unsubscribe(): any;
            unsubscribeAll(type: string): any;
        }
    
        interface ExecCommand {
            
            
        }
    
        interface Features {
            
            tests: any;
            
            all(cat: string, args: any[]): string;
            add(cat: string, name: string, o: any): any;
            test(cat: string, name: string, args: any[]): bool;
        }
    
        interface File {
            
            
        }
    
        interface Fills {
            
            
        }
    
        interface Get {
            
            Transaction: Get_TransactionStatic;
            cssOptions: any;
            jsOptions: any;
            options: any;
            
            css(urls: string, options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            css(urls: any, options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            css(urls: any[], options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            abort(transaction: Get_Transaction): any;
            js(urls: string, options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            js(urls: any, options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            js(urls: any[], options?: any, callback?: (err: any[], transaction: Get_Transaction) => any): Get_Transaction;
            load(urls: string, options?: any, callback?: Function, err?: any[], Transaction?: Get_Transaction): Get_Transaction;
            load(urls: string, options?: any, callback?: Function, err?: any, Transaction?: Get_Transaction): Get_Transaction;
            load(urls: any, options?: any, callback?: Function, err?: any[], Transaction?: Get_Transaction): Get_Transaction;
            load(urls: any, options?: any, callback?: Function, err?: any, Transaction?: Get_Transaction): Get_Transaction;
            load(urls: any[], options?: any, callback?: Function, err?: any[], Transaction?: Get_Transaction): Get_Transaction;
            load(urls: any[], options?: any, callback?: Function, err?: any, Transaction?: Get_Transaction): Get_Transaction;
            script(): any;
        }
    
        interface GetNodeJS {
            
            
            js(s: any[], options: any): any;
            js(s: string, options: any): any;
            script(): any;
        }
    
        interface Graph {
            
            
            getSeriesByKey(val: string): any;
            getSeriesByIndex(val: number): any;
        }
    
        interface Gridlines {
            
            
        }
    
        interface GroupDiamond {
            
            
        }
    
        interface GroupRect {
            
            
        }
    
        interface Handlebars {
            
            
            compile(string: string, options?: any): Function;
            log(level: string, message: string): any;
            precompile(string: string, options?: any): string;
            render(string: string, context: any, options?: any): string;
            registerPartial(name: string, partial: Function): any;
            registerPartial(name: string, partial: string): any;
            registerHelper(name: string, fn: Function, inverse?: bool): any;
            template(template: Function): Function;
        }
    
        interface Highlight {
            
            
            allCase(haystack: string, needles: string, options?: any): string;
            allCase(haystack: string, needles: String[], options?: any): string;
            all(haystack: string, needles: string, options?: any): string;
            all(haystack: string, needles: String[], options?: any): string;
            allFold(haystack: string, needles: string, options?: any): string;
            allFold(haystack: string, needles: String[], options?: any): string;
            start(haystack: string, needles: string, options?: any): string;
            start(haystack: string, needles: String[], options?: any): string;
            startCase(haystack: string, needles: string): string;
            startCase(haystack: string, needles: String[]): string;
            startFold(haystack: string, needles: string): string;
            startFold(haystack: string, needles: String[]): string;
            wordsFold(haystack: string, needles: string): string;
            wordsFold(haystack: string, needles: String[]): string;
            wordsCase(haystack: string, needles: string): string;
            wordsCase(haystack: string, needles: String[]): string;
            words(haystack: string, needles: string, options?: any): string;
            words(haystack: string, needles: String[], options?: any): string;
        }
    
        interface Histogram {
            
            
        }
    
        interface HistoryBase {
            
            SRC_REPLACE: string;
            SRC_ADD: string;
            NAME: string;
            force: bool;
            html5: bool;
            nativeHashChange: bool;
            
            addValue(key: string, value: string, options: any): HistoryBase;
            add(state: any, options: any): HistoryBase;
            get(key: string): any;
            get(key: string): string;
            replaceValue(key: string, value: string, options: any): HistoryBase;
            replace(state: any, options: any): HistoryBase;
        }
    
        interface HistoryHash {
            
            SRC_HASH: string;
            hashPrefix: string;
            
            decode(string: string): string;
            createHash(params: any): string;
            encode(string: string): string;
            getIframeHash(): string;
            getHash(): string;
            getUrl(): string;
            parseHash(hash: string): any;
            replaceHash(hash: string): any;
            setHash(hash: string): any;
        }
    
        interface HorizontalLegendLayout {
            
            
        }
    
        interface Intl {
            
            
            add(module: string, lang: string, strings: any): any;
            getAvailableLangs(module: string): any[];
            get(module: string, key: string, lang: string): any;
            lookupBestLang(preferredLanguages: String[], availableLanguages: String[]): string;
            lookupBestLang(preferredLanguages: string, availableLanguages: String[]): string;
            getLang(module: string): string;
            setLang(module: string, lang: string): any;
        }
    
        interface JSON {
            
            charCacheThreshold: number;
            _default: string;
            transports: any;
            useNativeStringify: bool;
            useNativeParse: bool;
            
            dateToString(d: Date): string;
            customTransport(id: string): any;
            defaultTransport(id?: string): any;
            notify(event: string, transaction: any, config: any): any;
            parse(s: string, reviver: Function): any;
            stringify(o: any, w: any[], ind: number): string;
            stringify(o: any, w: any[], ind: string): string;
            stringify(o: any, w: Function, ind: number): string;
            stringify(o: any, w: Function, ind: string): string;
        }
    
        interface JSONPRequest {
            
            
            _defaultCallback(url: string, config: any): Function;
            send(args: any): JSONPRequest;
        }
    
        interface Lang {
            
            
            now(): number;
            isValue(o: any): bool;
            isUndefined(o: any): bool;
            isString(o: any): bool;
            isObject(o: any, failfn: bool): bool;
            isNumber(o: any): bool;
            isNull(o: any): bool;
            isFunction(o: any): bool;
            isDate(o: any): bool;
            isBoolean(o: any): bool;
            isArray(o: any): bool;
            type(o: any): string;
            trimRight(s: string): string;
            trimLeft(s: string): string;
            trim(s: string): string;
            sub(s: string, o: any): string;
        }
    
        interface LeftAxisLayout {
            
            
        }
    
        interface LineSeries {
            
            
        }
    
        interface Lines {
            
            
        }
    
        interface MarkerSeries {
            
            
        }
    
        interface Mock {
            
            Value: Test_Mock_Value;
            
        }
    
        interface ModelSync {
            
            REST: ModelSync_REST;
            
        }
    
        interface ModelSync_REST {
            
            CSRF_TOKEN: string;
            EMULATE_HTTP: bool;
            HTTP_HEADERS: any;
            HTTP_METHODS: any;
            HTTP_TIMEOUT: number;
            root: string;
            url: string;
            
            getURL(action?: string, options?: any): string;
            parseIOResponse(response: any): any;
            serialize(action?: string): string;
            sync(action: string, options?: any, callback?: (err: Error, response?: any) => any): any;
        }
    
        interface Number {
            
            
            format(data: number, config: any): string;
            parse(data: string): number;
            parse(data: number): number;
            parse(data: bool): number;
        }
    
        interface NumericAxis {
            
            
            formatLabel(value: any, format: any): any;
            getLabelByIndex(i: number, l: number): any;
            getTotalByKey(key: string): any;
        }
    
        interface Object {
            
            
            each(obj: any, fn: (value: any, key: string, obj: any) => any, thisObj?: any, proto?: bool): YUI;
            isEmpty(obj: any): bool;
            getValue(o: any, path: any[]): any;
            hasValue(obj: any, value: any): bool;
            keys(obj: any): String[];
            hasKey(obj: any, key: string): bool;
            owns(obj: any, key: string): bool;
            setValue(o: any, path: any[], val: any): any;
            some(obj: any, fn: (value: any, key: string, obj: any) => any, thisObj?: any, proto?: bool): bool;
            size(obj: any): number;
            values(obj: any): any[];
        }
    
        interface Parallel {
            
            finished: number;
            results: any[];
            total: number;
            
            add(fn: Function): any;
            done(callback: (results: any, data?: any) => any, data: any): any;
            test(): any;
        }
    
        interface PieChart {
            
            
            _getAriaMessage(key: number): any;
            getSeriesItem(series: any, index: any): any;
        }
    
        interface PieSeries {
            
            
        }
    
        interface PjaxBase {
            
            
        }
    
        interface PjaxContent {
            
            
            loadContent(req: any, res: any, next: Function): any;
            getContent(responseText: string): any;
        }
    
        interface Plots {
            
            
        }
    
        interface Plugin {
            
            NodeFX: Plugin_NodeFX;
            EditorParaIE: Plugin_EditorParaIEStatic;
            EditorPara: Plugin_EditorParaStatic;
            EditorTab: Plugin_EditorTabStatic;
            EditorLists: Plugin_EditorListsStatic;
            EditorParaBase: Plugin_EditorParaBaseStatic;
            EditorBR: Plugin_EditorBRStatic;
            ExecCommand: Plugin_ExecCommandStatic;
            EditorBidi: Plugin_EditorBidiStatic;
            Flick: Plugin_Flick;
            CreateLinkBase: Plugin_CreateLinkBase;
            DDNodeScroll: Plugin_DDNodeScrollStatic;
            DDWindowScroll: Plugin_DDWindowScrollStatic;
            ScrollInfo: Plugin_ScrollInfo;
            Align: Plugin_Align;
            DDProxy: Plugin_DDProxyStatic;
            Shim: Plugin_Shim;
            Pjax: Plugin_Pjax;
            Base: Plugin_Base;
            Host: Plugin_Host;
            Drag: Plugin_DragStatic;
            ResizeConstrained: Plugin_ResizeConstrained;
            Drop: Plugin_DropStatic;
            Resize: Plugin_Resize;
            ResizeProxy: Plugin_ResizeProxyStatic;
            DDConstrained: Plugin_DDConstrainedStatic;
            ScrollViewList: Plugin_ScrollViewListStatic;
            ScrollViewPaginator: Plugin_ScrollViewPaginatorStatic;
            ScrollViewScrollbars: Plugin_ScrollViewScrollbarsStatic;
            SortScroll: Plugin_SortScrollStatic;
            DataTableDataSource: Plugin_DataTableDataSource;
            ConsoleFilters: Plugin_ConsoleFilters;
            CalendarNavigator: Plugin_CalendarNavigator;
            Cache: Plugin_Cache;
            AutoComplete: Plugin_AutoComplete;
            WidgetAnim: Plugin_WidgetAnim;
            
        }
    
        interface Plugin_Align {
            
            
            center(region: Node): any;
            center(region: HTMLElement): any;
            center(region: any): any;
            destroy(): any;
            to(region: string, regionPoint: string, point: string, resize: bool): any;
            to(region: Node, regionPoint: string, point: string, resize: bool): any;
            to(region: HTMLElement, regionPoint: string, point: string, resize: bool): any;
            to(region: any, regionPoint: string, point: string, resize: bool): any;
        }
    
        interface Plugin_AutoComplete {
            
            
        }
    
        interface Plugin_Base {
            
            NS: string;
            NAME: string;
            ATTRS: any;
            
            afterHostMethod(method: string, fn: Function, context: any): EventHandle;
            beforeHostMethod(method: string, fn: Function, context: any): EventHandle;
            afterHostEvent(type: string, fn: Function, context: any): EventHandle;
            afterHostEvent(type: any, fn: Function, context: any): EventHandle;
            doAfter(strMethod: string, fn: Function, context: any): EventHandle;
            doBefore(strMethod: string, fn: Function, context: any): EventHandle;
            destructor(): any;
            onHostEvent(type: string, fn: Function, context: any): EventHandle;
            onHostEvent(type: any, fn: Function, context: any): EventHandle;
            initializer(config: any): any;
        }
    
        interface Plugin_Cache {
            
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_CalendarNavigator {
            
            NS: string;
            NAME: string;
            ATTRS: any;
            
            destructor(): any;
            initializer(config: any): any;
        }
    
        interface Plugin_ConsoleFilters {
            
            NAME: string;
            NS: string;
            CATEGORIES_TEMPLATE: string;
            SOURCES_TEMPLATE: string;
            FILTER_TEMPLATE: string;
            
            hideCategory(cat: string): any;
            hideSource(src: string): any;
            refreshConsole(): any;
            showCategory(cat: string): any;
            showSource(src: string): any;
            syncUI(): any;
        }
    
        interface Plugin_CreateLinkBase {
            
            DEFAULT: any;
            PROMPT: any;
            STRINGS: any;
            
        }
    
        interface Plugin_DataTableDataSource {
            
            NS: string;
            NAME: string;
            
            onDataReturnInitializeTable(e: EventFacade): any;
            load(config: any): any;
        }
    
        interface Plugin_Flick {
            
            CLASS_NAMES: any;
            SNAP_EASING: string;
            EASING: string;
            SNAP_DURATION: number;
            VELOCITY_THRESHOLD: number;
            NS: string;
            NAME: string;
            transformMethods: any;
            
            compareTransformSequence(list1: any[], list2: any[]): any;
            decompose(_3x3: any[]): any;
            angle2rad(val: any): any;
            deg2rad(deg: number): any;
            getnxn(): any;
            getDeterminant(matrix: any[]): any;
            inverse(Array: any): any;
            getMinors(matrix: any[], columnIndex: number, rowIndex: number): any;
            getTransformArray(val: string): any;
            getTransformFunctionArray(): any;
            initializer(config: any): any;
            scalarMultiply(matrix: any[], multiplier: number): any;
            rad2deg(rad: number): any;
            setBounds(): any;
            sign(val: number): any;
            vectorMatrixProduct(vector: any[], matrix: any[]): any;
            transpose(matrix: any): any;
        }
    
        interface Plugin_Host {
            
            
            hasPlugin(ns: string): Plugin;
            plug(P: Function, config: any): Base;
            plug(P: any, config: any): Base;
            plug(P: any[], config: any): Base;
            unplug(plugin: string): Base;
            unplug(plugin: Function): Base;
        }
    
        interface Plugin_NodeFX {
            
            
        }
    
        interface Plugin_Pjax {
            
            
        }
    
        interface Plugin_Resize {
            
            ATTRS: any;
            NS: string;
            NAME: string;
            
        }
    
        interface Plugin_ResizeConstrained {
            
            constrainSurrounding: any;
            
        }
    
        interface Plugin_ScrollInfo {
            
            
            getScrollInfo(): any;
            getOnscreenNodes(selector?: string, margin?: number): NodeList;
            getOffscreenNodes(selector?: string, margin?: number): NodeList;
            refreshDimensions(): any;
        }
    
        interface Plugin_Shim {
            
            TEMPLATE: string;
            CLASS_NAME: string;
            
            destroy(): any;
            sync(): any;
        }
    
        interface Plugin_WidgetAnim {
            
            ATTRS: any;
            ANIMATIONS: any;
            NAME: string;
            NS: string;
            
            destructor(): any;
            initializer(config: any): any;
        }
    
        interface Pollable {
            
            
            clearInterval(id: number): any;
            clearAllIntervals(): any;
            setInterval(msec: number, request?: any): number;
        }
    
        interface Profiler {
            
            
            clear(name: string): undefined;
            getAverage(name: string): number;
            getFullReport(): any;
            getReport(): any;
            getFunctionReport(): any;
            getMin(name: string): number;
            getMax(name: string): number;
            getCallCount(name: string): number;
            instrument(name: string, method: Function): Function;
            getOriginal(name: string): Function;
            getOriginal(name: string): any;
            pause(name: string): undefined;
            registerConstructor(name: string, owner: any): undefined;
            registerFunction(name: string, owner: any, registerPrototype: bool): undefined;
            registerObject(name: string, owner: any, recurse: bool): undefined;
            start(name: string): undefined;
            stop(name: string): undefined;
            unregisterObject(name: string, recurse: bool): undefined;
            unregisterFunction(name: string): undefined;
        }
    
        interface QueryString {
            
            
            stringify(obj: any, cfg: any, name: string): any;
        }
    
        interface Record {
            
            
            getValue(field: string): any;
        }
    
        interface RecordsetFilter {
            
            
            filter(filter: Function, value?: any): Recordset;
            filter(filter: string, value?: any): Recordset;
            grep(pattern: RegExp): Recordset;
            reject(filter: Function): Recordset;
        }
    
        interface RecordsetIndexer {
            
            
            createTable(key: string): any;
            getTable(key: string): any;
        }
    
        interface RecordsetSort {
            
            
            flip(): any;
            resort(): any;
            reverse(): any;
            sort(field: string, desc: bool): any;
        }
    
        interface Renderer {
            
            
        }
    
        interface RightAxisLayout {
            
            
        }
    
        interface SVGCircle {
            
            
        }
    
        interface SVGDrawing {
            
            
            closePath(): any;
            clear(): any;
            curveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            end(): any;
            drawRect(x: number, y: number, w: number, h: number): any;
            lineTo(point1: number, point2: number): any;
            moveTo(x: number, y: number): any;
            relativeCurveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            quadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
            relativeLineTo(point1: number, point2: number): any;
            relativeMoveTo(x: number, y: number): any;
        }
    
        interface SVGEllipse {
            
            
        }
    
        interface SVGGraphic {
            
            
            batch(method: Function): any;
            clear(): any;
            addShape(cfg: any): any;
            destroy(): any;
            getXY(): any;
            getShapeById(id: string): any;
            removeShape(shape: Shape): any;
            removeShape(shape: string): any;
            removeAllShapes(): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
        }
    
        interface SVGPath {
            
            
        }
    
        interface SVGPieSlice {
            
            
        }
    
        interface SVGRect {
            
            
        }
    
        interface SVGShape {
            
            
            compareTo(refNode: HTMLElement): bool;
            compareTo(refNode: Node): bool;
            contains(needle: SVGShape): any;
            contains(needle: HTMLElement): any;
            addClass(className: string): any;
            destroy(): any;
            getXY(): any;
            getBounds(): any;
            removeClass(className: string): any;
            rotate(deg: number): any;
            scale(val: number): any;
            skewY(y: number): any;
            skewX(x: number): any;
            skew(x: number, y: number): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
            setXY(Contains: any[]): any;
            test(selector: string): any;
            translate(x: number, y: number): any;
            translateX(x: number): any;
            translateY(y: number): any;
            toFront(): any;
        }
    
        interface Selector {
            
            operators: any;
            shorthand: any;
            
            ancestor(element: HTMLElement, selector: string, testSelf: bool): HTMLElement;
            query(selector: string, root: HTMLElement, firstOnly: bool): any[];
        }
    
        interface SliderValueRange {
            
            
            getValue(): number;
            setValue(val: number): Slider;
        }
    
        interface SplineSeries {
            
            
        }
    
        interface StackedAreaSeries {
            
            
        }
    
        interface StackedAreaSplineSeries {
            
            
        }
    
        interface StackedAxis {
            
            
        }
    
        interface StackedBarSeries {
            
            
        }
    
        interface StackedColumnSeries {
            
            
        }
    
        interface StackedComboSeries {
            
            
        }
    
        interface StackedComboSplineSeries {
            
            
        }
    
        interface StackedLineSeries {
            
            
        }
    
        interface StackedMarkerSeries {
            
            
        }
    
        interface StackedSplineSeries {
            
            
        }
    
        interface StackingUtil {
            
            
        }
    
        module Test {
            
            class Console implements Test_Console { }
            var ArrayAssert: Test_ArrayAssert;
            var Assert: Test_Assert;

            class AssertionError implements Test_AssertionError {
                name: string;
                message: string;

                getMessage(): string;
                toString(): string;
            }

            class ComparisonFailure implements Test_ComparisonFailure {
                actual: any;
                expected: any;
                name: string;

                getMessage(): string;
            }

            class TestCase implements Test_TestCase {
                constructor(methods: any);
                assert(condition: bool, message: string): any;
                callback(): Function;
                destroy(): any;
                fail(message: string): any;
                init(): any;
                resume(segment: Function): undefined;
                setUp(): undefined;
                tearDown(): undefined;
                wait(segment: Function, delay: number): undefined;
            }

            class TestSuite implements Test_TestSuite {
                name: string;

                constructor(name: string);
                add(testObject: Test_TestSuite): undefined;
                add(testObject: Test_TestCase): undefined;
                setUp(): undefined;
                tearDown(): undefined;
            }

            var CoverageFormat: Test_CoverageFormat;
            var DateAssert: Test_DateAssert;
            var EventTarget: Test_EventTarget;
            var Mock: Test_Mock;
            var ObjectAssert: Test_ObjectAssert;
            var Reporter: Test_ReporterStatic;
            var Results: Test_Results;
            var ShouldError: Test_ShouldError;
            var ShouldFail: Test_ShouldFail;


            var TestFormat: Test_TestFormat;
            var TestRunner: Test_TestRunner;

            var TestNode: Test_TestNode;
            var Runner: Test_Runner;
            
            //UnexpectedError: Test_UnexpectedError;
            //UnexpectedValue: Test_UnexpectedValue;
            //Wait: Test_Wait;
            
        }
    
        interface Test_ArrayAssert {
            
            
            containsMatch(matcher: Function, haystack: any[], message: string): any;
            containsItems(needles: Object[], haystack: any[], message: string): any;
            contains(needle: any, haystack: any[], message: string): any;
            doesNotContainMatch(matcher: Function, haystack: any[], message: string): any;
            doesNotContainItems(needles: Object[], haystack: any[], message: string): any;
            doesNotContain(needle: any, haystack: any[], message: string): any;
            lastIndexOf(needle: any, haystack: any[], index: number, message: string): any;
            itemsAreSame(expected: any[], actual: any[], message: string): any;
            isNotEmpty(actual: any[], message: string): any;
            isEmpty(actual: any[], message: string): any;
            itemsAreEquivalent(expected: any[], actual: any[], comparator: Function, message: string): undefined;
            itemsAreEqual(expected: any[], actual: any[], message: string): any;
            indexOf(needle: any, haystack: any[], index: number, message: string): any;
        }
    
        interface Test_Assert {
            
            
            areSame(expected: any, actual: any, message: string): any;
            areNotSame(unexpected: any, actual: any, message: string): any;
            areNotEqual(unexpected: any, actual: any, message: string): any;
            areEqual(expected: any, actual: any, message: string): any;
            fail(message: string): any;
            isTypeOf(expectedType: string, actualValue: any, message: string): any;
            isString(actual: any, message: string): any;
            isObject(actual: any, message: string): any;
            isNumber(actual: any, message: string): any;
            isInstanceOf(expected: Function, actual: any, message: string): any;
            isFunction(actual: any, message: string): any;
            isBoolean(actual: any, message: string): any;
            isArray(actual: any, message: string): any;
            isUndefined(actual: any, message: string): any;
            isNull(actual: any, message: string): any;
            isNotUndefined(actual: any, message: string): any;
            isNotNull(actual: any, message: string): any;
            isNotNaN(actual: any, message: string): any;
            isNaN(actual: any, message: string): any;
            isTrue(actual: any, message: string): any;
            isFalse(actual: any, message: string): any;
            pass(): any;
            throwsError(expectedError: string, method: Function, message: string): undefined;
            throwsError(expectedError: Function, method: Function, message: string): undefined;
            throwsError(expectedError: any, method: Function, message: string): undefined;
        }
    
        interface Test_AssertionError {
            
            name: string;
            message: string;
            
            getMessage(): string;
            toString(): string;
        }
    
        interface Test_ComparisonFailure {
            
            actual: any;
            expected: any;
            name: string;
            
            getMessage(): string;
        }
    
        interface Test_Console {
            
            
        }
    
        interface Test_CoverageFormat {
            
            
            XdebugJSON(coverage: any): string;
            JSON(coverage: any): string;
        }
    
        interface Test_DateAssert {
            
            
            datesAreEqual(expected: Date, actual: Date, message: string): any;
            timesAreEqual(expected: Date, actual: Date, message: string): any;
        }
    
        interface Test_EventTarget {
            
            
            attach(type: string, listener: Function): undefined;
            detach(type: string, listener: Function): undefined;
            fire(event: any): undefined;
            fire(event: string): undefined;
            subscribe(type: string, listener: Function): undefined;
            unsubscribe(type: string, listener: Function): undefined;
        }
    
        interface Test_Mock {
            
            
            expect(mock: any, expectation: any): undefined;
            verify(mock: any): undefined;
        }
    
        interface Test_Mock_Value {
            
            Function: Function;
            Object: Function;
            String: Function;
            Number: Function;
            Boolean: Function;
            Any: Function;
            
        }
    
        interface Test_ObjectAssert {
            
            
            areEqual(expected: any, actual: any, message: string): any;
            ownsOrInheritsKeys(properties: any[], object: any, message: string): any;
            ownsOrInheritsKey(propertyName: string, object: any, message: string): any;
            ownsNoKeys(object: any, message: string): any;
            ownsKeys(properties: any[], object: any, message: string): any;
            ownsKey(propertyName: string, object: any, message: string): any;
            inheritsKeys(properties: any[], object: any, message: string): any;
            inheritsKey(propertyName: string, object: any, message: string): any;
            hasKeys(properties: any[], object: any, message: string): any;
            hasKey(propertyName: string, object: any, message: string): any;
        }
    
        interface Test_Results {
            
            duration: number;
            errors: number;
            failed: number;
            ignored: number;
            name: string;
            passed: number;
            total: number;
            
            include(result: Test_Results): undefined;
        }
    
        interface Test_Runner {
            
            
            clear(): undefined;
            add(testObject: any): undefined;
            getCoverage(format: Function): any;
            getCoverage(format: Function): string;
            getResults(format: Function): any;
            getResults(format: Function): string;
            isRunning(): bool;
            isWaiting(): bool;
            getName(): string;
            run(options: any): undefined;
            run(options: bool): undefined;
            resume(segment: Function): undefined;
            setName(name: string): undefined;
        }
    
        interface Test_ShouldError {
            
            name: string;
            
        }
    
        interface Test_ShouldFail {
            
            name: string;
            
        }
    
        interface Test_TestCase {
            
            
            assert(condition: bool, message: string): any;
            callback(): Function;
            destroy(): any;
            fail(message: string): any;
            init(): any;
            resume(segment: Function): undefined;
            setUp(): undefined;
            tearDown(): undefined;
            wait(segment: Function, delay: number): undefined;
        }
    
        interface Test_TestFormat {
            
            
            TAP(result: any): string;
            JUnitXML(result: any): string;
            XML(result: any): string;
            JSON(result: any): string;
        }
    
        interface Test_TestNode {
            
            
        }
    
        interface Test_TestRunner {
            
            
        }
    
        interface Test_TestSuite {
            
            name: string;
            
            add(testObject: Test_TestSuite): undefined;
            add(testObject: Test_TestCase): undefined;
            setUp(): undefined;
            tearDown(): undefined;
        }
    
        interface Test_UnexpectedError {
            
            cause: Error;
            name: string;
            stack: string;
            
        }
    
        interface Test_UnexpectedValue {
            
            name: string;
            unexpected: any;
            
            getMessage(): string;
        }
    
        interface Test_Wait {
            
            delay: number;
            segment: Function;
            
        }
    
        interface Text {
            
            AccentFold: Text_AccentFold;
            WordBreak: Text_WordBreak;
            
        }
    
        interface Text_AccentFold {
            
            
            compare(a: string, b: string, func: Function): bool;
            canFold(string: string): bool;
            fold(input: string): string;
            fold(input: any[]): string;
            fold(input: string): any[];
            fold(input: any[]): any[];
            filter(haystack: any[], func: Function): any[];
        }
    
        interface Text_WordBreak {
            
            
            isWordBoundary(string: string, index: number): bool;
            getUniqueWords(string: string, options: any): any[];
            getWords(string: string, options: any): any[];
        }
    
        interface TimeAxis {
            
            
            formatLabel(value: any, format: any): any;
            getLabelByIndex(i: number, l: number): any;
        }
    
        interface TopAxisLayout {
            
            
        }
    
        interface UA {
            
            caja: number;
            accel: bool;
            android: number;
            air: number;
            chrome: number;
            gecko: number;
            nodejs: number;
            os: string;
            ios: bool;
            ipod: number;
            iphone: number;
            ipad: number;
            mobile: string;
            opera: number;
            ie: number;
            phantomjs: number;
            safari: number;
            secure: bool;
            silk: number;
            userAgent: string;
            webos: number;
            webkit: number;
            
            compareVersions(a: number, b: number): any;
            compareVersions(a: number, b: string): any;
            compareVersions(a: string, b: number): any;
            compareVersions(a: string, b: string): any;
            parseUA(subUA?: string): any;
        }
    
        interface Uploader {
            
            Queue: Uploader_QueueStatic;
            TYPE: string;
            
        }
    
        interface VMLCircle {
            
            
        }
    
        interface VMLDrawing {
            
            
            clear(): any;
            closePath(): any;
            curveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            end(): any;
            drawRect(x: number, y: number, w: number, h: number): any;
            lineTo(point1: number, point2: number): any;
            moveTo(x: number, y: number): any;
            relativeCurveTo(cp1x: number, cp1y: number, cp2x: number, cp2y: number, x: number, y: number): any;
            quadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
            relativeQuadraticCurveTo(cpx: number, cpy: number, x: number, y: number): any;
            relativeLineTo(point1: number, point2: number): any;
            relativeMoveTo(x: number, y: number): any;
        }
    
        interface VMLEllipse {
            
            
        }
    
        interface VMLGraphic {
            
            
            batch(method: Function): any;
            clear(): any;
            addShape(cfg: any): any;
            destroy(): any;
            getXY(): any;
            getShapeById(id: string): any;
            removeShape(shape: Shape): any;
            removeShape(shape: string): any;
            removeAllShapes(): any;
            setPosition(x: number, y: number): any;
            setSize(w: number, h: number): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
        }
    
        interface VMLPath {
            
            
        }
    
        interface VMLPieSlice {
            
            
        }
    
        interface VMLRect {
            
            
        }
    
        interface VMLShape {
            
            
            compareTo(refNode: HTMLElement): bool;
            compareTo(refNode: Node): bool;
            contains(needle: VMLShape): any;
            contains(needle: HTMLElement): any;
            addClass(className: string): any;
            destroy(): any;
            getXY(): any;
            getBounds(): any;
            scale(val: number): any;
            rotate(deg: number): any;
            removeClass(className: string): any;
            setXY(Contains: any[]): any;
            skew(x: number, y: number): any;
            skewX(x: number): any;
            skewY(y: number): any;
            set(name: string, value: any): any;
            set(name: any, value: any): any;
            test(selector: string): any;
            translate(x: number, y: number): any;
            translateX(x: number): any;
            translateY(y: number): any;
            toFront(): any;
        }
    
        interface ValueChange {
            
            TIMEOUT: number;
            POLL_INTERVAL: number;
            
        }
    
        interface VerticalLegendLayout {
            
            
        }
    
        interface View_NodeMap {
            
            
            getByNode(node: Node): View;
            getByNode(node: HTMLElement): View;
            getByNode(node: string): View;
        }
    
        interface WidgetAutohide {
            
            ATTRS: any;
            
            _afterHideOnChange(): any;
        }
    
        interface WidgetButtons {
            
            DEFAULT_BUTTONS_SECTION: string;
            BUTTONS_TEMPLATE: string;
            BUTTONS: any;
            NON_BUTTON_NODE_CFG: any[];
            CLASS_NAMES: any;
            
            addButton(button: (action?: Function, classNames?: string, context?: any, disabled?: bool, events?: string, isDefault?: bool, label?: string, name?: string, section?: string, srcNode?: Node, template?: string) => any, section?: string, index?: number): WidgetButtons;
            getButton(name: number, section?: string): Node;
            getButton(name: string, section?: string): Node;
            removeButton(button: Node, section?: string): WidgetButtons;
            removeButton(button: number, section?: string): WidgetButtons;
            removeButton(button: string, section?: string): WidgetButtons;
        }
    
        interface WidgetChild {
            
            ROOT_TYPE: any;
            
        }
    
        interface WidgetModality {
            
            STACK: any;
            ATTRS: any;
            
            _afterFocusOnChange(): any;
            _repositionMask(nextElem: Widget): any;
            _afterHostZIndexChangeModal(e: EventFacade): any;
            _afterHostVisibleChangeModal(e: EventFacade): any;
            _detachUIHandlesModal(): any;
            _attachUIHandlesModal(): any;
            _uiSetHostZIndexModal(ZIndex: number): any;
            _uiSetHostVisibleModal(Whether: bool): any;
            _getMaskNode(): Node;
            _blur(): any;
            _focus(): any;
            _GET_MASK(): any;
            isNested(): any;
        }
    
        interface WidgetPosition {
            
            POSITIONED_CLASS_NAME: string;
            ATTRS: any;
            
            move(x: number, y: number): any;
            syncXY(): any;
        }
    
        interface WidgetPositionConstrain {
            
            ATTRS: any;
            
            getConstrainedXY(xy: any[], node: Node): any[];
            getConstrainedXY(xy: any[], node: bool): any[];
        }
    
        interface WidgetStack {
            
            SHIM_TEMPLATE: string;
            STACKED_CLASS_NAME: string;
            SHIM_CLASS_NAME: string;
            HTML_PARSER: any;
            ATTRS: any;
            
            sizeShim(): any;
        }
    
        interface WidgetStdMod {
            
            TEMPLATES: any;
            SECTION_CLASS_NAMES: any;
            HTML_PARSER: any;
            ATTRS: any;
            REPLACE: string;
            BEFORE: string;
            AFTER: string;
            FOOTER: string;
            BODY: string;
            HEADER: string;
            
            getStdModNode(section: string, forceCreate: bool): Node;
            setStdModContent(section: string, content: string, where: string): any;
            setStdModContent(section: string, content: Node, where: string): any;
        }
    
        interface XML {
            
            
            format(data: any): string;
            parse(data: string): any;
        }
    
        interface config {
            
            delayUntil: string;
            cacheUse: bool;
            _2in3: string;
            aliases: any;
            cssAttributes: string;
            combine: bool;
            comboBase: string;
            base: string;
            dateFormat: string;
            core: any[];
            debug: bool;
            bootstrap: bool;
            errorFn: Function;
            gallery: string;
            fetchCSS: bool;
            force: string[];
            filters: any;
            filter: string;
            doc: Document;
            loadErrorFn: Function;
            logFn: Function;
            loaderPath: string;
            groups: any;
            modules: any;
            onCSS: Function;
            jsAttributes: string;
            insertBefore: string;
            ignore: string[];
            locale: string;
            lang: string;
            injected: bool;
            logExclude: any;
            logInclude: any;
            pollInterval: number;
            requireRegistration: bool;
            root: string;
            purgethreshold: number;
            skin: any;
            timeout: number;
            throwFail: bool;
            useNativeES5: bool;
            useHistoryHTML5: bool;
            useBrowserConsole: bool;
            windowResizeDelay: number;
            win: Window;
            yui2: string;
            
        }
    
        interface plugin {
            
            NodeFocusManager: plugin_NodeFocusManager;
            NodeMenuNav: plugin_NodeMenuNav;
            
        }
    
        interface plugin_NodeFocusManager {
            
            
            blur(): any;
            focus(index: number): any;
            refresh(): any;
            start(): any;
            stop(): any;
        }
    
        interface plugin_NodeMenuNav {
            
            SHIM_TEMPLATE: string;
            SHIM_TEMPLATE_TITLE: string;
            
        }
    
        class Promise {
            constructor(lambda: any);
            then(fulfilled: any, rejected?: any): Promise;
            getStatus(): string;
            static batch(...proms: Promise[]): Promise;
        }
}

declare var YUI: Y.YUIStatic;