@bootstrapParser
module lang::rascal::ide::Outline

import ParseTree;
import lang::rascal::\syntax::Rascal;
import Node;
import Map;
import List;
 
anno str node@label;
anno loc node@\loc;
anno loc FunctionDeclaration@\loc;
anno loc Declaration@\loc;
anno loc Name@\loc;
anno loc QualifiedName@\loc;
anno loc Signature@\loc;
anno loc Prod@\loc;

node outline(start[Module] m) = outline(m.top);
 
node outline(Module m) {
   n = "<m.header.name>";
   aliases = [];
   annotations = [];
   functions = ();
   imports = [];
   grammars = ();
   tags = [];
   tests = ();
   adts = ();
   variables = []; 
   list[node] e = [];
   
   top-down-break visit (m) {
     case (Declaration) `<Tags ta> <Visibility vs> <Type t> <{Variable ","}+ vars>;`:
       variables   += [clean("<v.name> <t>")()[@\loc=v@\loc] | v <- vars]; 
     case (Declaration) `<Tags ta> <Visibility vs> anno <Type t> <Type ot>@<Name name>;`:  
       annotations += [clean("<name> <t> <ot>@<name>")()[@\loc=name@\loc]];
     case (Declaration) `<Tags ta> <Visibility vs> alias <UserType u> = <Type base>;`:
       aliases += [clean("<u.name>")()[@\loc=u.name@\loc]];  
     case (Declaration) `<Tags ta> <Visibility vs> tag <Kind k> <Name name> on <{Type ","}+ types>;`:
       tags += [clean("<name>")()[@\loc=name@\loc]];
       
     case (Declaration) `<Tags ta> <Visibility vs> data <UserType u> <CommonKeywordParameters kws>;`: {
       f = "<u.name>";
       c = adts["<u.name>"]?e;
       
       if (kws is present) {
         c += [ ".<k.name> <k.\type>"()[@\loc=k@\loc] | KeywordFormal k <- kws.keywordFormalList];
       }
       
       adts[f] = c;
     }
     
     case (Declaration) `<Tags ta> <Visibility vs> data <UserType u> <CommonKeywordParameters kws> = <{Variant "|"}+ variants>;` : {
       f = "<u.name>";
       c = adts[f]?e;
       
       if (kws is present) {
         c += [ ".<k.name> <k.\type>"()[@\loc=k@\loc] | k <- kws.keywordFormalList];
       }
       
       c += [ clean("<v>")()[@\loc=v@\loc] | v <- variants];
       
       adts[f] = c;
     }
     
     case FunctionDeclaration func : {
       f = clean("<func.signature.name>")()[@label="<func.signature.name> <func.signature.parameters>"][@\loc=func.signature@\loc];
       
       if (/(FunctionModifier) `test` := func.signature) {
         tests[clean("<func.signature.name>")]?e += [f];
       }
       else {
         functions[clean("<func.signature.name>")]?e += [f];
       }
     }
     
     case (Import) `extend <ImportedModule mm>;` :
       imports += ["<mm.name>"()[@\loc=mm@\loc]];
       
     case (Import) `import <ImportedModule mm>;` :
       imports += ["<mm.name>"()[@\loc=mm@\loc]];
       
     case (Import) `import <QualifiedName m2> = <LocationLiteral at>;` :
       imports += ["<m2>"()[@\loc=m2@\loc]];
       
     case SyntaxDefinition def : {
       f = "<def.defined>";
       c = grammars[f]?e;
       c += ["<p>"()[@label="<prefix><p.syms>"][@\loc=p@\loc] 
                    | /Prod p := def.production, p is labeled || p is unlabeled,
                      str prefix := (p is labeled ? "<p.name>: " : "")
                    ];
       grammars[f] = c;
     }    
   }

   map[node,list[node]] count(map[str,list[node]] m)
     = ((!isEmpty(m[k]) ? "<k> (<size(m[k])>)"()[@\loc=(m[k][0])@\loc] : "<k> (<size(m[k])>)"()) : m[k] | k <- m);
     
   return n(
      "Functions"(count(functions))[@label="Functions (<size(functions)>)"],
      "Tests"(count(tests))[@label="Tests (<size(tests)>)"],
      "Variables"(variables)[@label="Variables (<size(variables)>)"],
      "Aliases"(aliases)[@label="Aliases (<size(aliases)>)"],
      "Data"(count(adts))[@label="Data (<size(adts)>)"],
      "Annotations"(annotations)[@label="Annotations (<size(annotations)>)"],
      "Tags"(tags)[@label="Tags (<size(tags)>)"],
      "Imports"(imports)[@label="Imports (<size(imports)>)"],
      "Syntax"(count(grammars))[@label="Syntax (<size(grammars)>)"]
   );    
}

str clean(/\\<rest:.*>/) = rest;
default str clean(str x) = x;