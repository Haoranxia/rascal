 module lang::rascal::tests::functionality::Annotation
  /*******************************************************************************
   * Copyright (c) 2009-2015 CWI
   * All rights reserved. This program and the accompanying materials
   * are made available under the terms of the Eclipse Public License v1.0
   * which accompanies this distribution, and is available at
   * http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
  
   *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
   *   * Paul Klint - Paul.Klint@cwi.nl - CWI
  *******************************************************************************/
 
data F = f() | f(int n) | g(int n) | deep(F f);
anno int F@pos;
data AN = an(int n);
  	
// boolannotations
  		
test bool boolannotations1() = f() [@pos=1] == f();
test bool boolannotations2() = f() [@pos=1]@pos == 1;
test bool boolannotations3() = f() [@pos=1][@pos=2]@pos == 2;
  		
test bool boolannotations4() = f(5) [@pos=1] == f(5);
test bool boolannotations5() = f(5) [@pos=1]@pos == 1;
test bool boolannotations6() = f(5) [@pos=1][@pos=2]@pos == 2;
  		
test bool boolannotations7() = deep(f(5) [@pos=1]) == deep(f(5));
test bool boolannotations8() = f(5) [@pos=1] == f(5) [@pos=2];	
  	
// annotationsInSets
  
test bool annotationsInSets1() = {f() [@pos=1]} == {f()};
test bool annotationsInSets2() = {f() [@pos=1], g(2) [@pos=2]} == {f(), g(2)};
test bool annotationsInSets3() = {f() [@pos=1], g(2)} == {f(), g(2)[@pos=2]};		
test bool annotationsInSets4() = {deep(f(5) [@pos=1])} == {deep(f(5))};
  	
test bool annotationsInSets5() = {f() [@pos=1]} + {g(2) [@pos=2]} == {f(), g(2)};
  		
test bool annotationsInSets6() = {X = {f() [@pos=1]} + {f() [@pos=2]}; {F elem} := X && (elem@pos == 2 || elem@pos == 1);};
