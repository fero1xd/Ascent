package me.fero.ascent.objects.config;

import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SystemConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);
    private static Components components;
    private static List<Cpu> cpus;

    static {
        components = JSensors.get.components();
        cpus = components.cpus;
    }

    public static List<Temperature> getCpuTemperature() {
        if(cpus == null) {
            LOGGER.debug("No cpus detected! Returning -1");
            return null;
        }

        for(final Cpu cpu : cpus) {
            System.out.println(cpu.name);
            if(cpu.sensors != null) {
                System.out.println("Found censors");
                return cpu.sensors.temperatures;
            }
        }

        return null;
    }
}
