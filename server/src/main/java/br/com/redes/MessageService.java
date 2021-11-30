package br.com.redes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

class MessageService implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    public static final String BEARER_TWITTER = "Bearer AAAAAAAAAAAAAAAAAAAAAPjBWAEAAAAAcwpt423TkIVLZcfh14L1BnoeV6A%3DZrhfrA7gwNWvnMlQEqWT2AZQMTDP6WeZHoLlt5itMkHkbOm0zL";


    private final Socket client;
    private final String clientMessage;
    private final Gson gson;
    private OutputStream outputStream = null;


    /**
     * Método responsável por inicializar a classe com as informações que foram recebidas do cliente
     *
     * @param socket
     * @throws IOException
     */
    public MessageService(Socket socket) throws IOException {
        this.client = socket;
        this.outputStream = client.getOutputStream();
        this.clientMessage = getMessageFromSocketInput(socket);
        this.gson = new Gson();
    }

    /**
     * Método responsável por processar o dado a partir da mensagem recebida
     */
    @Override
    public void run() {
        log.info(clientMessage);
        String content = getDataByClientMessage(clientMessage);
        sendDataToClient(content);
    }

    /**
     * Método responsável por transformar a mensagem recebida em bytes para textos
     *
     * @param socket
     * @return
     * @throws IOException
     */
    private String getMessageFromSocketInput(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            String output = new String(buffer, 0, read);
            return output;
        }
        return "";
    }

    /**
     * Método responsável por identificar o que deve ser executado ou não a partir da mensagem do cliente
     *
     * @param clientMessage
     * @return
     */
    private String getDataByClientMessage(String clientMessage) {
        switch (clientMessage) {
            case "/quem":
                return getHostname();
            case "/data":
                return getServerDate();
            case "/ip":
                return getServerIp();
            case "/mac":
                return getMac();
            case "/sys":
                return getSO();
            case "/dev":
                return "Anderson Rodrigues (amaik@mx2.unisc.br)";
            case "/info":
                return getInfo();
            case "/dolar":
                return getUSDValue();
            case "/trends":
                return getTrends();
        }
        return "Mensagem não identificada";
    }

    /**
     * Método responsável por buscar 10 posts de um usuário no twitter
     *
     * @return
     */
    private String getTrends() {
        URL url = null;
        HttpURLConnection con = null;
        StringBuffer content = new StringBuffer();
        try {
            url = new URL("https://api.twitter.com/2/users/390321157/tweets");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", BEARER_TWITTER);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }
        return getContentFormatted(content);
    }

    /**
     * Método responsável por formatar o retorno recebido da API do Twitter
     *
     * @param content
     * @return
     */
    private String getContentFormatted(StringBuffer content) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("10 últimos twetts da timeline do Anderson Rodrigues").append(System.lineSeparator());
            JsonObject jsonObject = gson.fromJson(content.toString(), JsonObject.class);
            int count;
            jsonObject.get("data").getAsJsonArray().forEach(item -> {
                JsonObject obj = (JsonObject) item;
                sb.append(obj.get("text")).append(System.lineSeparator());
            });
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Método responsável por retornar as informações gerais do servidor como: info do disco, memória e processador
     *
     * @return
     */
    private String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb = putDiskInfo(sb);

        sb.append(System.lineSeparator());
        sb.append("Memory: ").append(Runtime.getRuntime().totalMemory());

        sb.append(System.lineSeparator());
        sb.append("Procesadores: ").append(Runtime.getRuntime().availableProcessors());

        sb.append(System.lineSeparator());
        sb = putProcessorsTemperatureInfo(sb);

        return sb.toString();
    }

    /**
     * Método responsável por processar as informações do disco local do servidor
     *
     * @param sb
     * @return
     */
    private StringBuilder putDiskInfo(StringBuilder sb) {
        File fileC = new File("c:");
        long totalC = fileC.getTotalSpace() / 1024 / 1024 / 1024;
        long usedC = fileC.getFreeSpace() / 1024 / 1024 / 1024;
        sb.append("Disco C: (Total/Livre): ").append(totalC).append("/").append(usedC);
        return sb;
    }

    /**
     * Método responsável por buscar e processar as informações de temperatura do processador
     *
     * @param sb
     * @return
     */
    private StringBuilder putProcessorsTemperatureInfo(StringBuilder sb) {
        Components components = JSensors.get.components();
        List<Cpu> cpus = components.cpus;
        if (cpus != null) {
            for (final Cpu cpu : cpus) {
                sb.append("Found CPU component: ").append(cpu.name).append(System.lineSeparator());
                if (cpu.sensors != null) {
                    //Print temperatures
                    List<Temperature> temps = cpu.sensors.temperatures;
                    for (final Temperature temp : temps) {
                        sb.append(temp.name).append(": ").append(temp.value).append(" C").append(System.lineSeparator());
                    }
                }
            }
        }
        return sb;
    }

    /**
     * Método responsável por buscar o S.O. do servidor
     *
     * @return
     */
    private String getSO() {
        String os = System.getProperty("os.name");
        return os;
    }

    /**
     * Método responsável por buscar o MAC do servidor.
     *
     * @return
     */
    private String getMac() {
        var mac = "";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            byte[] hardwareAddress = ni.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            mac = String.join("-", hexadecimal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }

    /**
     * Método responsável por buscar a cotação atual do dolar em BRL
     *
     * @return
     */
    private String getUSDValue() {
        BufferedReader in = null;
        var content = "";
        try {
            URL requestIp = new URL("https://economia.awesomeapi.com.br/last/USD");
            in = new BufferedReader(new InputStreamReader(
                    requestIp.openStream()));
            content = in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro on get dolar value");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return getDolarValueFromContent(content);
    }

    /**
     * Método responsável por processar o retorno recebido da API de consulta de cotação do dolar
     *
     * @param content
     * @return
     */
    private String getDolarValueFromContent(String content) {
        var value = "";
        try {
            JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
            JsonObject jsonObjectDolarProperties = gson.fromJson(jsonObject.get("USDBRL").toString(), JsonObject.class);
            value = jsonObjectDolarProperties.get("high") + " " + jsonObjectDolarProperties.get("codein");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Método responsável responsável por retornar as informações sobre IP do servidor.
     *
     * @return
     */
    private String getServerIp() {
        var ips = "";
        ips += getLocalIp();
        ips += getExternalIp();
        return ips;
    }

    /**
     * Método responsável por retornar informações do IP externo do servidor.
     *
     * @return
     */
    private String getExternalIp() {
        try {
            var ip = getExternalIpWithAWS();
            return "External: " + ip;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro on get External IP from server");
        }
        return "";
    }

    /**
     * Método responsável por processar as informações de IP externo do servidor através da API de checkip da Amazon
     *
     * @return
     * @throws IOException
     */
    private String getExternalIpWithAWS() throws IOException {
        URL requestIp = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    requestIp.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Método responsável por buscar o ip interno do servidor
     *
     * @return
     */
    private String getLocalIp() {
        try {
            String[] localHost = InetAddress.getLocalHost().toString().split("/");
            return "Internal: " + localHost[1] + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro on get Local IP from server");
        }
        return "";
    }

    /**
     * Método responsável por buscar e formatar a data atual do servidor
     *
     * @return
     */
    private String getServerDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            var mills = System.currentTimeMillis();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mills);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error on get time from system");
        }
        return "";
    }

    /**
     * Método responsável por buscar o hostname do servidor
     *
     * @return
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error on get hostname");
        }
        return "";
    }

    /**
     * Método responsável por enviar a informação que foi processada a partir da mensagem
     * do cliente de volta para o cliente
     *
     * @param data
     */
    private void sendDataToClient(String data) {
        byte[] buffer = data.getBytes();
        try {
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
            client.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
