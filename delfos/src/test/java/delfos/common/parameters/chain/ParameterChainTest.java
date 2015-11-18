/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class ParameterChainTest {

    public ParameterChainTest() {
    }

    /**
     * Test of createWithNode method, of class ParameterChain.
     */
    @Test
    public void testCreateWithNode() {
        System.out.println("createWithNode");
        Parameter parameter = null;
        ParameterOwner parameterOwner = null;
        ParameterChain instance = null;
        ParameterChain expResult = null;
        ParameterChain result = instance.createWithNode(parameter, parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createWithLeaf method, of class ParameterChain.
     */
    @Test
    public void testCreateWithLeaf() {
        System.out.println("createWithLeaf");
        Parameter parameter = null;
        Object parameterValue = null;
        ParameterChain instance = null;
        ParameterChain expResult = null;
        ParameterChain result = instance.createWithLeaf(parameter, parameterValue);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of areCompatible method, of class ParameterChain.
     */
    @Test
    public void testAreCompatible() {
        System.out.println("areCompatible");
        List<ParameterChain> parameterChains = null;
        boolean expResult = false;
        boolean result = ParameterChain.areCompatible(parameterChains);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of areSame method, of class ParameterChain.
     */
    @Test
    public void testAreSame() {
        System.out.println("areSame");
        List<ParameterChain> parameterChains = null;
        boolean expResult = false;
        boolean result = ParameterChain.areSame(parameterChains);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createAllTerminalParameterChains method, of class ParameterChain.
     */
    @Test
    public void testCreateAllTerminalParameterChains() {
        System.out.println("createAllTerminalParameterChains");
        String rootName = "";
        ParameterOwner parameterOwner = null;
        ParameterChain instance = null;
        List<ParameterChain> expResult = null;
        List<ParameterChain> result = instance.createAllTerminalParameterChains(rootName, parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
