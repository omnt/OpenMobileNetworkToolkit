package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import junit.framework.TestCase;

public class Iperf3AdapterTest extends TestCase {
    Iperf3Adapter iperf3 = new Iperf3Adapter();
    public void testStartProcess() {
        assertEquals(0, this.iperf3.startProcess("iperf3 -c localhost -t 5"));
    }

    public void testGetOutputStream() {
        System.out.println(this.iperf3.getOutputString());
    }
}