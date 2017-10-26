package postscript;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

/**
 * PSGr1 is a Graphics subclass for Java 1.1 that images to PostScript.
 * (C) 1996 E.J. Friedman-Hill and Sandia National Labs
 * @version 	2.1
 * @author 	Ernest Friedman-Hill
 * @author      ejfried@ca.sandia.gov
 * @author      http://herzberg.ca.sandia.gov
 */

public class PSGr1 extends PSGrBase
{
  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @param o Output stream for PostScript output
   * @see #create
   */

  public PSGr1()
  {
    super();
  }

  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @param o Output stream for PostScript output
   * @see #create
   */

  public PSGr1(Writer o)
  {
    super(o, true);
  }

  /**
   * Constructs a new PSGr1 Object. Unlike regular Graphics objects,
   * PSGr contexts can be created directly.
   * @param o Output stream for PostScript output
   * @see #create
   */

  public PSGr1(Writer o, boolean emitProlog)
  {
    super(o, emitProlog);
  }

}


  
