package com.jdmm.quasar;

import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.logica.LogicaSatellite;
import com.microsoft.azure.functions.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for Function class.
 */
public class FunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test
    public void testHttpTriggerJava() throws Exception {
       LogicaSatellite logicaSatellite = new LogicaSatellite();
       SatelliteIn satellite = new SatelliteIn();
       satellite.setDistance(100.0);
       List<String> listaMensajes = new ArrayList<>();
       listaMensajes.add("hola");
       listaMensajes.add("");
       satellite.setMessage(listaMensajes);
       satellite.setName("kenobi");
       SatelliteIn satellite2 = new SatelliteIn();
       satellite2.setDistance(115.5);
       List<String> listaMensajes2 = new ArrayList<>();
       listaMensajes2.add("");
       listaMensajes2.add("mundo");
       satellite2.setMessage(listaMensajes2);
       satellite2.setName("skywalker");
       SatellitesIn satellites = new SatellitesIn();
       List<SatelliteIn> listaSatellite = new ArrayList<>();
       listaSatellite.add(satellite2);				
       listaSatellite.add(satellite);
       satellites.setSatellites(listaSatellite);
       ResultadoUbicacion resultado = logicaSatellite.calcularUbicacion(satellites);
       System.out.println("||| mensaje "+resultado.getMensaje()+" "+resultado.getPosicionX()+" "+resultado.getPosicionY());
       assertEquals(resultado.getPosicionX(),-226.16405812370527);
       assertEquals(resultado.getPosicionY(),0.0);
    }
}


