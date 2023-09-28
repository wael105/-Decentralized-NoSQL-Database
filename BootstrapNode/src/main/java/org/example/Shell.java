package org.example;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public enum Shell {

    INSTANCE;

    public String runCommand(String command) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();

        StringConsumer output = new StringConsumer();

        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), output);

        Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

        process.waitFor();

        future.get(10, TimeUnit.SECONDS);
        return output.getString();
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}

class StringConsumer implements Consumer<String> {
    private final StringBuilder theString;

    public StringConsumer() {
        theString = new StringBuilder();
    }

    @Override
    public void accept(String s) {
        theString.append(s);
    }

    public String getString() {
        return theString.toString();
    }
}