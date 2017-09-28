package com.taykey.bwb.plugins;

import com.taykey.bwb.core.base.BasePlugin;
import com.taykey.bwb.core.definitions.ClientResult;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.core.definitions.ResultAggregator;
import com.taykey.bwb.core.impl.clients.PlainSSHClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by matan on 26/09/2017.
 */
public class WrkPlugin extends BasePlugin<PlainSSHClient>{

    @Override
    public void setup(RemoteCluster<PlainSSHClient> cluster) {
        cluster.run("sudo apt-get install wrk -y");
    }

    @Override
    public String getName() {
        return "wrk";
    }

    @Override
    public ResultAggregator<String> getResultAggregator() {
        return new WrkResultAggregator();
    }

    @Override
    protected String unitOfWork(RemoteCluster<PlainSSHClient> cluster, Map<String, String> args) {

        //Do we have a script file?
        if(args.containsKey("-s")){
            //Upload the file
            try {
                String script = new String(Files.readAllBytes(Paths.get(args.get("-s"))));
                String remotepath = "/tmp/script.lua";
                cluster.run(String.format("rm %s", remotepath));
                cluster.run(String.format("echo '%s' > %s", script, remotepath));

                return String.format("wrk -c %s -t %s -d %ss -s %s --latency %s",
                        args.get("-c"),
                        args.get("-t"),
                        args.get("-d"),
                        remotepath,
                        args.get("-u"));

            } catch (IOException e) {
                e.printStackTrace();
                return "echo cannot run command, problem when loading the script";
            }
        }
        else{
            return String.format("wrk -c %s -t %s -d %ss --latency %s",
                    args.get("-c"),
                    args.get("-t"),
                    args.get("-d"),
                    args.get("-u"));
        }
    }

    @Override
    public String help() {
        StringJoiner sj = new StringJoiner("\n");

        sj.add("Wrk - Http Benchmark Tool");
        sj.add("Options:");
        sj.add("  -c => number of open connections to user.");
        sj.add("  -t => number of threads.");
        sj.add("  -u => the url of the remote host.");
        sj.add("  -d => the duration of the benchmark in seconds.");
        sj.add("  -s => the path to the script to be uploaded.");
        sj.add("Example: plugin-run -p wrk -c cluster -o \"-c 10 -t 2 -d 10s -s s.lua http://localhost:8200/test\"");


        return sj.toString();
    }

    public class WrkResultAggregator implements ResultAggregator<String> {
        @Override
        public String aggregate(List<ClientResult> resultList) {

            WrkResult finalResult = new WrkResult();

            resultList.stream()
                    .map(r-> parseRawResult(r.getCommandResult()))
                    .forEach(r->{
                        finalResult.setMaxLatency(Math.max(finalResult.getMaxLatency(), r.getMaxLatency()));
                        finalResult.setAvgLatency(finalResult.getAvgLatency() + r.getAvgLatency());
                        finalResult.setPercentile50(finalResult.getPercentile50() + r.getPercentile50());
                        finalResult.setPercentile75(finalResult.getPercentile75() + r.getPercentile75());
                        finalResult.setPercentile90(finalResult.getPercentile90() + r.getPercentile90());
                        finalResult.setPercentile99(finalResult.getPercentile99() + r.getPercentile99());
                        finalResult.setTotalTraffic(finalResult.getTotalTraffic() + r.getTotalTraffic());
                        finalResult.setTotalRequests(finalResult.getTotalRequests() + r.getTotalRequests());
                        finalResult.setErrors(finalResult.getErrors() + r.getErrors());
                        finalResult.setRps(finalResult.getRps() + r.getRps());
                        finalResult.setTps(finalResult.getTps() + r.getTps());
                    }
            );

            //Divide the ones that need to be average:
            finalResult.setAvgLatency(finalResult.getAvgLatency() / resultList.size());
            finalResult.setPercentile50(finalResult.getPercentile50() / resultList.size());
            finalResult.setPercentile75(finalResult.getPercentile75() / resultList.size());
            finalResult.setPercentile90(finalResult.getPercentile90() / resultList.size());
            finalResult.setPercentile99(finalResult.getPercentile99() / resultList.size());

            return finalResult.toString();
        }

        private WrkResult parseRawResult(String input){
            //Reduce multiple whitespaces to one:
            String output = input.replaceAll("( )+", " ");

            //Split by lines:
            List<String> split = Arrays.asList(output.split("\n"));

            //Trim each line:
            List<String> list = split.stream()
                    .map(line->line = line.trim())
                    .collect(Collectors.toList());

            //Error will return only if occurs:
            String errors = "0";
            if(list.get(11).startsWith("Non-2xx")){
                errors = list.get(11).split(" ")[4];
            }

            //Build the result:
            WrkResult result = new WrkResult();
            result.setMaxLatency(parseDuration(list.get(3).split(" ")[3]));
            result.setAvgLatency(parseDuration(list.get(3).split(" ")[1]));
            result.setPercentile50(parseDuration(list.get(6).split(" ")[1]));
            result.setPercentile75(parseDuration(list.get(7).split(" ")[1]));
            result.setPercentile90(parseDuration(list.get(8).split(" ")[1]));
            result.setPercentile99(parseDuration(list.get(9).split(" ")[1]));
            result.setTotalRequests(Integer.valueOf(list.get(10).split(" ")[0]));
            result.setTotalTraffic(parseTraffic(list.get(10).split(" ")[4]));
            result.setErrors(Integer.valueOf(errors));
            result.setRps(Float.valueOf(list.get(list.size()-2).split(" ")[1]).intValue());
            result.setTps(parseTraffic(list.get(list.size() - 1).split(" ")[1]));

            return result;
        }

        private double parseDuration(String durationString){
            double durationMicro = 0;
            if(durationString.contains("us")){
                String duration = durationString.replace("us", "");
                durationMicro = Double.parseDouble(duration);
            }
            else if(durationString.contains("ms")){
                String duration = durationString.replace("ms", "");
                durationMicro = Double.parseDouble(duration) * 1000;
            }
            else if(durationString.contains("s")){
                String duration = durationString.replace("ms", "");
                durationMicro = Double.parseDouble(duration) * 1000 * 1000;
            }

            return durationMicro;
        }

        private float parseTraffic(String trafficString){
            float trafficMB = 0;
            if(trafficString.contains("MB")){
                String traffic = trafficString.replace("MB", "");
                trafficMB = Float.parseFloat(traffic);
            }
            else if(trafficString.contains("GB")){
                String traffic = trafficString.replace("GB", "");
                trafficMB = Float.parseFloat(traffic) * 1024;
            }
            else if(trafficString.contains("KB")){
                String traffic = trafficString.replace("KB", "");
                trafficMB = Float.parseFloat(traffic) / 1024;
            }

            return trafficMB;
        }

    }

    public class WrkResult{

        private double maxLatency;
        private double avgLatency;
        private double percentile50, percentile75, percentile90, percentile99;
        private int totalRequests;
        private float totalTraffic;
        private int errors;
        private int rps;
        private float tps;

        @Override
        public String toString() {
            try{
                StringJoiner sj = new StringJoiner("\n");

                sj.add(String.format("%d total requests has been sent and %.2fMB network traffic was used.", totalRequests, totalTraffic));
                sj.add(String.format("Requests per second: %d.", rps));
                sj.add(String.format("Traffic per second: %.2fMB.", tps));
                sj.add(String.format("The longest request took: %.2fus", maxLatency));
                sj.add(String.format("The average request took: %.2fus", avgLatency));
                sj.add("Latency distribution:");
                sj.add(String.format("  50 -> %.2fus", percentile50));
                sj.add(String.format("  75 -> %.2fus", percentile75));
                sj.add(String.format("  90 -> %.2fus", percentile90));
                sj.add(String.format("  99 -> %.2fus", percentile99));

                if(errors>0){
                    sj.add(String.format("Total errors (non 2xx or 3xx): %d", errors));
                }

                return sj.toString();
            }
            catch (Exception e){
                return super.toString();
            }
        }

        public WrkResult() {
        }

        public double getMaxLatency() {
            return maxLatency;
        }

        public void setMaxLatency(double maxLatency) {
            this.maxLatency = maxLatency;
        }

        public double getAvgLatency() {
            return avgLatency;
        }

        public void setAvgLatency(double avgLatency) {
            this.avgLatency = avgLatency;
        }

        public double getPercentile50() {
            return percentile50;
        }

        public void setPercentile50(double percentile50) {
            this.percentile50 = percentile50;
        }

        public double getPercentile75() {
            return percentile75;
        }

        public void setPercentile75(double percentile75) {
            this.percentile75 = percentile75;
        }

        public double getPercentile90() {
            return percentile90;
        }

        public void setPercentile90(double percentile90) {
            this.percentile90 = percentile90;
        }

        public double getPercentile99() {
            return percentile99;
        }

        public void setPercentile99(double percentile99) {
            this.percentile99 = percentile99;
        }

        public int getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(int totalRequests) {
            this.totalRequests = totalRequests;
        }

        public float getTotalTraffic() {
            return totalTraffic;
        }

        public void setTotalTraffic(float totalTraffic) {
            this.totalTraffic = totalTraffic;
        }

        public int getErrors() {
            return errors;
        }

        public void setErrors(int errors) {
            this.errors = errors;
        }

        public int getRps() {
            return rps;
        }

        public void setRps(int rps) {
            this.rps = rps;
        }

        public float getTps() {
            return tps;
        }

        public void setTps(float tps) {
            this.tps = tps;
        }
    }

}
