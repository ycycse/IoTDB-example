package iotdb.webDemo.controller;

import iotdb.webDemo.Tool;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Controller
public class DemoController {

    private Session session;
    private String DeviceId;
    private long startTime = -1;

    private final String HOST = "YOUR HOST";
    private final String USERNAME = "root";
    private final String PASSWORD = "root";
    private final int PORT = 6667;


    public void Connect() throws IoTDBConnectionException, StatementExecutionException {
        session = new Session(HOST,PORT,USERNAME,PASSWORD);
        session.open();

        DeviceId = "root.demo.cpu";

    }

    @RequestMapping("/CPU")
    @ResponseBody
    public String getCPUInfo() throws IoTDBConnectionException, StatementExecutionException {

        Connect();

        Timer timer = new Timer();
        startTime = System.currentTimeMillis();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String usage = Tool.getCPUInfo();
                List<String> measurements = new ArrayList<>();
                List<String> values = new ArrayList<>();

                measurements.add("usage");
                values.add(usage);
                try {
                    session.insertRecord(DeviceId,System.currentTimeMillis(),measurements,values);
                } catch (IoTDBConnectionException e) {
                    e.printStackTrace();
                } catch (StatementExecutionException e) {
                    e.printStackTrace();
                }
            }
        },0,1000);


        return Long.toString(startTime);
    }

    @RequestMapping("/")
    public String showPage(){
        return "usage";
    }


    @RequestMapping("/result")
    @ResponseBody
    public List<String> showResult() throws IoTDBConnectionException, StatementExecutionException {
        List<String> ret = new ArrayList<>();

        List<String> path = new ArrayList<>();
        path.add("root.demo.cpu.usage");


        SessionDataSet sessionDataSet = session.executeRawDataQuery(path,startTime,System.currentTimeMillis());
        if(sessionDataSet==null){
            return ret;
        }
        startTime = System.currentTimeMillis();


        while (sessionDataSet.hasNext()){
            SessionDataSet.DataIterator dataIterator = sessionDataSet.iterator();
            List<String> names = sessionDataSet.getColumnNames();


            String stringBuilder = new Timestamp(Long.parseLong(dataIterator.getString("Time"))) +
                    "          " + dataIterator.getString("root.demo.cpu.usage");

            ret.add(stringBuilder);
        }

        return ret;
    }


}
