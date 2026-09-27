package app.capibara.batterylens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Read-only Shizuku user service, running as ADB shell. */
public final class ProbeService extends IProbe.Stub {
    public ProbeService() { }

    @Override public String read() {
        StringBuilder out = new StringBuilder();
        String[] supplies = {"battery", "bms"};
        String[] fields = {"cycle_count", "charge_full", "charge_full_design", "charge_counter",
                "capacity", "temp", "voltage_now", "current_now", "current_avg", "soh", "state_of_health", "qmax"};
        for (String supply : supplies) for (String field : fields) {
            File file = new File("/sys/class/power_supply/" + supply + "/" + field);
            if (!file.isFile()) continue;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String value = reader.readLine();
                if (value != null && value.matches("-?[0-9]{1,15}"))
                    out.append(supply).append('.').append(field).append('=').append(value).append('\n');
            } catch (Exception ignored) { /* Driver or SELinux may deny this node. */ }
        }
        return out.toString();
    }
}
