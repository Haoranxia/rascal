/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/

package org.rascalmpl.ast;


import org.eclipse.imp.pdb.facts.IConstructor;

import org.rascalmpl.interpreter.asserts.Ambiguous;

import org.eclipse.imp.pdb.facts.IValue;

import org.rascalmpl.interpreter.IEvaluatorContext;

import org.rascalmpl.interpreter.Evaluator;


import org.rascalmpl.interpreter.env.Environment;

import org.rascalmpl.interpreter.matching.IBooleanResult;

import org.rascalmpl.interpreter.matching.IMatchingResult;

import org.rascalmpl.interpreter.result.Result;


public abstract class Case extends AbstractAST {
  public Case(IConstructor node) {
    super(node);
  }
  

  public boolean hasPatternWithAction() {
    return false;
  }

  public org.rascalmpl.ast.PatternWithAction getPatternWithAction() {
    throw new UnsupportedOperationException();
  }

  public boolean hasStatement() {
    return false;
  }

  public org.rascalmpl.ast.Statement getStatement() {
    throw new UnsupportedOperationException();
  }


static public class Ambiguity extends Case {
  private final java.util.List<org.rascalmpl.ast.Case> alternatives;

  public Ambiguity(IConstructor node, java.util.List<org.rascalmpl.ast.Case> alternatives) {
    super(node);
    this.alternatives = java.util.Collections.unmodifiableList(alternatives);
  }

  @Override
  public Result<IValue> interpret(Evaluator __eval) {
    throw new Ambiguous(this.getTree());
  }
  
  @Override
  public org.eclipse.imp.pdb.facts.type.Type typeOf(Environment env) {
    throw new Ambiguous(this.getTree());
  }
  
  @Override
  public IBooleanResult buildBacktracker(IEvaluatorContext __eval) {
    throw new Ambiguous(this.getTree());
  }

  @Override
  public IMatchingResult buildMatcher(IEvaluatorContext __eval) {
    throw new Ambiguous(this.getTree());
  }
  
  public java.util.List<org.rascalmpl.ast.Case> getAlternatives() {
   return alternatives;
  }

  public <T> T accept(IASTVisitor<T> v) {
	return v.visitCaseAmbiguity(this);
  }
}





  public boolean isPatternWithAction() {
    return false;
  }
  
static public class PatternWithAction extends Case {
  // Production: sig("PatternWithAction",[arg("org.rascalmpl.ast.PatternWithAction","patternWithAction")])

  
     private final org.rascalmpl.ast.PatternWithAction patternWithAction;
  

  
public PatternWithAction(IConstructor node , org.rascalmpl.ast.PatternWithAction patternWithAction) {
  super(node);
  
    this.patternWithAction = patternWithAction;
  
}


  @Override
  public boolean isPatternWithAction() { 
    return true; 
  }

  @Override
  public <T> T accept(IASTVisitor<T> visitor) {
    return visitor.visitCasePatternWithAction(this);
  }
  
  
     @Override
     public org.rascalmpl.ast.PatternWithAction getPatternWithAction() {
        return this.patternWithAction;
     }
     
     @Override
     public boolean hasPatternWithAction() {
        return true;
     }
  	
}


  public boolean isDefault() {
    return false;
  }
  
static public class Default extends Case {
  // Production: sig("Default",[arg("org.rascalmpl.ast.Statement","statement")])

  
     private final org.rascalmpl.ast.Statement statement;
  

  
public Default(IConstructor node , org.rascalmpl.ast.Statement statement) {
  super(node);
  
    this.statement = statement;
  
}


  @Override
  public boolean isDefault() { 
    return true; 
  }

  @Override
  public <T> T accept(IASTVisitor<T> visitor) {
    return visitor.visitCaseDefault(this);
  }
  
  
     @Override
     public org.rascalmpl.ast.Statement getStatement() {
        return this.statement;
     }
     
     @Override
     public boolean hasStatement() {
        return true;
     }
  	
}



}
