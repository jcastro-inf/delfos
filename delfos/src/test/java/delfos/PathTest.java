package delfos;

import delfos.Path;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.Global;
import delfos.constants.DelfosTest;

/**
 * Implementa test para comprobar que la transformación de directorios es
 * correcta.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version Unknown date
 * @version 15-Octubre-2013
 */
public class PathTest extends DelfosTest {

    public PathTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Global.setVerbose();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of relativizePath method, of class Path.
     */
    @Test
    public void testRelativizePath() {

        Global.showMessage("relativizePath\n");

        File base = new File("D:" + File.separator + "Dropbox" + File.separator + "Carpeta1" + File.separator + "Carpeta2");

        //Comprobar si este segmento de código solo funciona bajo una plataforma windows.
//        //Dispositivo distinto
//        {
//            String result = Path.relativizePath(base, new File("C:" + File.separator + "Dropbox" + File.separator + "Carpeta1"));
//            String expResult = "C:" + File.separator + "Dropbox" + File.separator + "Carpeta1";
//            assertEquals(expResult, result);
//        }
        //Ruta relativa dentro
        {
            String result = Path.relativizePath(base, new File("D:" + File.separator + "Dropbox" + File.separator + "Carpeta1" + File.separator + "Carpeta2" + File.separator + "Carpeta3"));

            Global.showMessage(result + "\n");
            String expResult = new File("Carpeta3").getAbsolutePath();
            result = new File(result).getAbsolutePath();
            assertEquals(expResult, result);
        }

        //Ruta relativa fuera
        {
            String result = Path.relativizePath(base, new File("D:" + File.separator + "Dropbox"));
            String expResult = ".." + File.separator + ".." + File.separator + "";
            assertEquals(expResult, result);
        }

        //Ruta relativa fuera y avanzando
        {
            //D:"+File.separator+"Dropbox"+File.separator+"Carpeta1"+File.separator+"Carpeta2
            //D:"+File.separator+"Dropbox"+File.separator+"CarpetaHermana
            String result = Path.relativizePath(base, new File("D:" + File.separator + "Dropbox" + File.separator + "CarpetaHermana"));
            String expResult = ".." + File.separator + ".." + File.separator + "CarpetaHermana";
            assertEquals(expResult, result);
        }
    }
}
