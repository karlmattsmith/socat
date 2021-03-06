package gov.noaa.pmel.dashboard.ferret;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class SocatTool extends Thread {

    private FerretConfig ferret;
    private ArrayList<String> scriptArgs;
    private String expocode;
    private FerretConfig.Action action;
    private String message;
    private boolean error;
    private boolean done;


    public SocatTool(FerretConfig ferretConf) {
        ferret = new FerretConfig();
        ferret.setRootElement(ferretConf.getRootElement().clone());
        scriptArgs = new ArrayList<String>(3);
        expocode = null;
        message = null;
        error = false;
        done = false;
    }

    public void init(List<String> scriptArgs, String expocode, FerretConfig.Action action) {
        this.scriptArgs.clear();
        this.scriptArgs.addAll(scriptArgs);
        this.expocode = expocode;
        this.action = action;
    }

    @Override
    public void run() {
        done = false;
        error = false;
        PrintStream script_writer;
        try {

            String temp_dir = ferret.getTempDir();
            if ( !temp_dir.endsWith(File.separator) )
                temp_dir = temp_dir + "/";

            String driver = ferret.getDriverScript(action);

            File temp = new File(temp_dir);
            if ( !temp.exists() ) {
                temp.mkdirs();
            }

            File script;
            if ( action.equals(FerretConfig.Action.COMPUTE) ) {
                script = new File(temp_dir, "ferret_compute_" + expocode + ".jnl");
            }
            else if ( action.equals(FerretConfig.Action.DECIMATE) ) {
                script = new File(temp_dir, "ferret_decimate_" + expocode + ".jnl");
            }
            else if ( action.equals(FerretConfig.Action.PLOTS) ) {
                script = new File(temp_dir, "ferret_plots_" + expocode + ".jnl");
            }
            else
                throw new RuntimeException("Unknown action " + action.toString());

            script_writer = new PrintStream(script);
            script_writer.print("go " + driver);
            for (String scarg : scriptArgs) {
                script_writer.print(" \"" + scarg + "\"");
            }
            script_writer.println();

            List<String> args = ferret.getArgs();
            String interpreter = ferret.getInterpreter();
            String executable = ferret.getExecutable();
            String[] fullCmd;
            int offset;
            if ( (interpreter != null) && !interpreter.equals("") ) {
                fullCmd = new String[args.size() + 3];
                fullCmd[0] = interpreter;
                fullCmd[1] = executable;
                offset = 2;
            }
            else {
                fullCmd = new String[args.size() + 2];
                fullCmd[0] = executable;
                offset = 1;
            }
            for (int index = 0; index < args.size(); index++) {
                String arg = args.get(index);
                fullCmd[offset + index] = arg;
            }

            fullCmd[args.size() + offset] = script.getAbsolutePath();

            long timelimit = ferret.getTimeLimit();

            Task task = new Task(fullCmd, ferret.getRuntimeEnvironment().getEnv(),
                    new File(temp_dir), new File("cancel"), timelimit, ferret.getErrorKeys());
            task.run();
            error = task.getHasError();
            message = task.getErrorMessage();
            done = true;
            if ( !error )
                script.delete();
        } catch ( Exception e ) {
            done = true;
            error = true;
            message = e.getMessage();
        }


    }

    public boolean hasError() {
        return error;
    }

    public String getErrorMessage() {
        return message;
    }

    public boolean isDone() {
        return done;
    }
}
