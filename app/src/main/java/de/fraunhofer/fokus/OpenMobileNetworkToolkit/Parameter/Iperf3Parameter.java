/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Iperf3Parameter extends Parameter {
    public static final String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    public static final String rawDirPath = rootPath+"/omnt/iperf3/raw/";
    public static final String lineProtocolDirPath = rootPath+"/omnt/iperf3/lineprotocol/";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String BITRATE = "bitrate";
    public static final String INTERVAL = "interval";
    public static final String BYTES = "bytes";
    public static final String STREAMS = "streams";
    public static final String DIRECTION = "direction";
    public static final String ONEOFF = "oneOff";
    public static final String CPORT = "cport";
    public static final String TESTUUID = "testUUID";
    public static final String USERNAME = "username";
    public static final String RSAPUBLICKEYPATH = "rsaPublicKeyPath";
    public static final String EXTRADATA = "extraData";
    public static final String TITLE = "title";
    public static final String OMIT = "omit";
    public static final String ZEROCOPY = "zerocopy";
    public static final String FLOWLABEL = "flowlabel";
    public static final String DSCP = "dscp";
    public static final String TOS = "tos";
    public static final String VERSION6 = "version6";
    public static final String VERSION4 = "version4";
    public static final String NODELAY = "noDelay";
    public static final String SETMSS = "setMss";
    public static final String CONGESTION = "congestion";
    public static final String WINDOW = "window";
    public static final String PARALLEL = "parallel";
    public static final String BLOCKCOUNT = "blockcount";
    public static final String TIME = "time";
    public static final String FQRATE = "fqRate";
    public static final String PACINGTIMER = "pacingTimer";
    public static final String CONNECTTIMEOUT = "connectTimeout";
    public static final String UDP = "udp";
    public static final String XBIND = "xbind";
    public static final String SCTP = "sctp";
    public static final String USEPKCS1PADDING = "usePkcs1Padding";
    public static final String TIMESKEWTHRESHOLD = "timeSkewThreshold";
    public static final String AUTHORIZEDUSERSPATH = "authorizedUsersPath";
    public static final String RSAPRIVATEKEYPATH = "rsaPrivateKeyPath";
    public static final String IDLETIMEOUT = "idleTimeout";
    public static final String SERVERBITRATELIMIT = "serverBitrateLimit";

    public static final String SERVER = "server";
    public static final String CLIENT = "client";
    public static final String DAEMON = "daemon";
    public static final String HELP = "help";
    public static final String VERSION = "version";
    public static final String DEBUG = "debug";
    public static final String SNDTIMEOUT = "sndTimeout";
    public static final String RCVTIMEOUT = "rcvTimeout";
    public static final String TIMESTAMPS = "timestamps";
    public static final String FORCEFLUSH = "forceflush";
    public static final String LOGFILE = "logfile";

    public static final String JSON = "json";
    public static final String VERBOSE = "verbose";
    public static final String BINDDEV = "bindDev";
    public static final String BIND = "bind";
    public static final String AFFINITY = "affinity";
    public static final String FILE = "file";
    public static final String MODE = "mode";
    public static final String PROTOCOL = "protocol";
    public static final String IPERF3UUID = "iperf3UUID";

    private static final String TAG = "Iperf3Parameter";


    protected Iperf3Parameter(Parcel in) {
        super(in);
        host = in.readString();
        iPerf3UUID = in.readString();
        port = in.readInt();
        interval = in.readDouble();
        bitrate = in.readString();
        length = in.readInt();
        pidfile = in.readString();
        file = in.readString();
        affinity = in.readString();
        bind = in.readString();
        bindDev = in.readString();
        byte tmpVerbose = in.readByte();
        verbose = tmpVerbose == 0 ? null : tmpVerbose == 1;
        byte tmpJson = in.readByte();
        json = tmpJson == 0 ? null : tmpJson == 1;
        byte tmpJsonStream = in.readByte();
        jsonStream = tmpJsonStream == 0 ? null : tmpJsonStream == 1;
        byte tmpForceflush = in.readByte();
        forceflush = tmpForceflush == 0 ? null : tmpForceflush == 1;
        timestamps = in.readString();
        if (in.readByte() == 0) {
            rcvTimeout = null;
        } else {
            rcvTimeout = in.readInt();
        }
        if (in.readByte() == 0) {
            sndTimeout = null;
        } else {
            sndTimeout = in.readInt();
        }
        if (in.readByte() == 0) {
            debug = null;
        } else {
            debug = in.readInt();
        }
        byte tmpVersion = in.readByte();
        version = tmpVersion == 0 ? null : tmpVersion == 1;
        byte tmpHelp = in.readByte();
        help = tmpHelp == 0 ? null : tmpHelp == 1;
        byte tmpDaemon = in.readByte();
        daemon = tmpDaemon == 0 ? null : tmpDaemon == 1;
        byte tmpOneOff = in.readByte();
        oneOff = tmpOneOff == 0 ? null : tmpOneOff == 1;
        serverBitrateLimit = in.readString();
        if (in.readByte() == 0) {
            idleTimeout = null;
        } else {
            idleTimeout = in.readInt();
        }
        rsaPrivateKeyPath = in.readString();
        authorizedUsersPath = in.readString();
        if (in.readByte() == 0) {
            timeSkewThreshold = null;
        } else {
            timeSkewThreshold = in.readInt();
        }
        byte tmpUsePkcs1Padding = in.readByte();
        usePkcs1Padding = tmpUsePkcs1Padding == 0 ? null : tmpUsePkcs1Padding == 1;
        byte tmpSctp = in.readByte();
        sctp = tmpSctp == 0 ? null : tmpSctp == 1;
        xbind = in.readString();
        if (in.readByte() == 0) {
            nstreams = null;
        } else {
            nstreams = in.readInt();
        }
        if (in.readByte() == 0) {
            connectTimeout = null;
        } else {
            connectTimeout = in.readInt();
        }
        pacingTimer = in.readString();
        fqRate = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readInt();
        }
        bytes = in.readString();
        blockcount = in.readString();
        if (in.readByte() == 0) {
            cport = null;
        } else {
            cport = in.readInt();
        }
        if (in.readByte() == 0) {
            parallel = null;
        } else {
            parallel = in.readInt();
        }
        byte tmpReverse = in.readByte();
        reverse = tmpReverse == 0 ? null : tmpReverse == 1;
        byte tmpBidir = in.readByte();
        bidir = tmpBidir == 0 ? null : tmpBidir == 1;
        window = in.readString();
        congestion = in.readString();
        if (in.readByte() == 0) {
            setMss = null;
        } else {
            setMss = in.readInt();
        }
        byte tmpNoDelay = in.readByte();
        noDelay = tmpNoDelay == 0 ? null : tmpNoDelay == 1;
        byte tmpVersion4 = in.readByte();
        version4 = tmpVersion4 == 0 ? null : tmpVersion4 == 1;
        byte tmpVersion6 = in.readByte();
        version6 = tmpVersion6 == 0 ? null : tmpVersion6 == 1;
        if (in.readByte() == 0) {
            tos = null;
        } else {
            tos = in.readInt();
        }
        dscp = in.readString();
        if (in.readByte() == 0) {
            flowlabel = null;
        } else {
            flowlabel = in.readInt();
        }
        byte tmpZerocopy = in.readByte();
        zerocopy = tmpZerocopy == 0 ? null : tmpZerocopy == 1;
        if (in.readByte() == 0) {
            omit = null;
        } else {
            omit = in.readInt();
        }
        title = in.readString();
        extraData = in.readString();
        byte tmpGetServerOutput = in.readByte();
        getServerOutput = tmpGetServerOutput == 0 ? null : tmpGetServerOutput == 1;
        byte tmpUdpCounters64bit = in.readByte();
        udpCounters64bit = tmpUdpCounters64bit == 0 ? null : tmpUdpCounters64bit == 1;
        byte tmpRepeatingPayload = in.readByte();
        repeatingPayload = tmpRepeatingPayload == 0 ? null : tmpRepeatingPayload == 1;
        byte tmpDontFragment = in.readByte();
        dontFragment = tmpDontFragment == 0 ? null : tmpDontFragment == 1;
        username = in.readString();
        rsaPublicKeyPath = in.readString();
    }

    public static final Creator<Iperf3Parameter> CREATOR = new Creator<Iperf3Parameter>() {
        @Override
        public Iperf3Parameter createFromParcel(Parcel in) {
            return new Iperf3Parameter(in);
        }

        @Override
        public Iperf3Parameter[] newArray(int size) {
            return new Iperf3Parameter[size];
        }
    };

    public Iperf3Parameter(String ip,
                           int port,
                           String bitrate,
                           int duration,
                           double interval,
                           String bytes,
                           int streams,
                           int cport,
                           String testUUID,
                           Iperf3Mode mode,
                           Iperf3Protocol protocol,
                           Iperf3Direction direction
                           ) {

        super(rawDirPath+testUUID+".txt", lineProtocolDirPath+testUUID+".txt");
        this.testUUID = testUUID;
        this.host = ip;
        this.port = port;
        this.bitrate = bitrate;
        this.time = duration;
        this.interval = interval;
        this.bytes = bytes;
        this.nstreams = streams;
        this.cport = cport;
        this.mode = mode;
        this.direction = direction;
        this.protocol = protocol;

    }

    public Iperf3Parameter(String iPerf3UUID){
        super(rawDirPath+iPerf3UUID+".txt", lineProtocolDirPath+iPerf3UUID+".txt");
    }
    public Iperf3Parameter(String ip,
                           String iPerf3UUID,
                           Iperf3Protocol protocol,
                           int port,
                           double interval,
                           String bitrate,
                           int length,
                           Iperf3Mode mode,
                           Iperf3Direction direction,
                           String pidfile,
                           String file,
                           String affinity,
                           String bind,
                           String bindDev,
                           Boolean verbose,
                           Boolean json,
                           Boolean jsonStream,
                           String logfile,
                           Boolean forceflush,
                           String timestamps,
                           Integer rcvTimeout,
                           Integer sndTimeout,
                           Integer debug,
                           Boolean version,
                           Boolean help,
                           Boolean daemon,
                           Boolean oneOff,
                           String serverBitrateLimit,
                           Integer idleTimeout,
                           String rsaPrivateKeyPath,
                           String authorizedUsersPath,
                           Integer timeSkewThreshold,
                           Boolean usePkcs1Padding,
                           Boolean sctp,
                           String xbind,
                           Integer nstreams,
                           Integer connectTimeout,
                           String pacingTimer,
                           String fqRate,
                           Integer time,
                           String bytes,
                           String blockcount,
                           Integer cport,
                           Integer parallel,
                           Boolean reverse,
                           Boolean bidir,
                           String window,
                           String congestion,
                           Integer setMss,
                           Boolean noDelay,
                           Boolean version4,
                           Boolean version6,
                           Integer tos,
                           String dscp,
                           Integer flowlabel,
                           Boolean zerocopy,
                           Integer omit,
                           String title,
                           String extraData,
                           Boolean getServerOutput,
                           Boolean udpCounters64bit,
                           Boolean repeatingPayload,
                           Boolean dontFragment,
                           String username,
                           String rsaPublicKeyPath) {
        super(rawDirPath+iPerf3UUID+".txt", lineProtocolDirPath+iPerf3UUID+".txt");
        this.host = ip;
        this.iPerf3UUID = iPerf3UUID;
        this.protocol = protocol;
        this.port = port;
        this.interval = interval;
        this.bitrate = bitrate;
        this.length = length;
        this.mode = mode;
        this.direction = direction;
        this.pidfile = pidfile;
        this.file = file;
        this.affinity = affinity;
        this.bind = bind;
        this.bindDev = bindDev;
        this.verbose = verbose;
        this.json = json;
        this.jsonStream = jsonStream;
        this.forceflush = forceflush;
        this.timestamps = timestamps;
        this.rcvTimeout = rcvTimeout;
        this.sndTimeout = sndTimeout;
        this.debug = debug;
        this.version = version;
        this.help = help;
        this.daemon = daemon;
        this.oneOff = oneOff;
        this.serverBitrateLimit = serverBitrateLimit;
        this.idleTimeout = idleTimeout;
        this.rsaPrivateKeyPath = rsaPrivateKeyPath;
        this.authorizedUsersPath = authorizedUsersPath;
        this.timeSkewThreshold = timeSkewThreshold;
        this.usePkcs1Padding = usePkcs1Padding;
        this.sctp = sctp;
        this.xbind = xbind;
        this.nstreams = nstreams;
        this.connectTimeout = connectTimeout;
        this.pacingTimer = pacingTimer;
        this.fqRate = fqRate;
        this.time = time;
        this.bytes = bytes;
        this.blockcount = blockcount;
        this.cport = cport;
        this.parallel = parallel;
        this.reverse = reverse;
        this.bidir = bidir;
        this.window = window;
        this.congestion = congestion;
        this.setMss = setMss;
        this.noDelay = noDelay;
        this.version4 = version4;
        this.version6 = version6;
        this.tos = tos;
        this.dscp = dscp;
        this.flowlabel = flowlabel;
        this.zerocopy = zerocopy;
        this.omit = omit;
        this.title = title;
        this.extraData = extraData;
        this.getServerOutput = getServerOutput;
        this.udpCounters64bit = udpCounters64bit;
        this.repeatingPayload = repeatingPayload;
        this.dontFragment = dontFragment;
        this.username = username;
        this.rsaPublicKeyPath = rsaPublicKeyPath;
    }

    public void updatePaths(){
        super.setLogfile(rawDirPath+testUUID+".txt");
        super.setLineProtocolFile(lineProtocolDirPath+testUUID+".txt");
    }
    public Iperf3Parameter(JSONObject jsonObject, String testUUID) {
        super(rawDirPath+testUUID+".txt", lineProtocolDirPath+testUUID+".txt");

        this.testUUID = testUUID;
        this.jsonStream = true;

        try {
            this.host = jsonObject.getString(HOST);
        } catch (JSONException e) {
            return;
        }
        try {
            this.iPerf3UUID = jsonObject.getString(IPERF3UUID);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Iperf3Parameter: iPerf3UUID is required.");
        }
        try {
            this.port = jsonObject.getInt(PORT);
        } catch (JSONException e) {
            Log.d(TAG, "port is not set. Defaulting to iPerf3 default Port.");
        }
        try {
            this.bitrate = jsonObject.getString(BITRATE);
        } catch (JSONException e) {
            Log.d(TAG, "bitrate is not set. Defaulting to iPerf3 default bitrate.");
        }
        try {
            this.time = jsonObject.getInt(TIME);
            Log.d(TAG, "Iperf3Parameter: time: "+time);
        } catch (JSONException e) {
            Log.d(TAG, "time is not set. Defaulting to iPerf3 default time.");
        }

        try {
            this.interval = jsonObject.getDouble(INTERVAL);
        } catch (JSONException e) {
            Log.d(TAG, "interval is not set. Defaulting to iPerf3 default interval.");
        }
        try {
            this.length = jsonObject.getInt(BYTES);
        } catch (JSONException e) {
            Log.d(TAG, "Length not set.");
        }
        try {
            this.nstreams = jsonObject.getInt(STREAMS);
        } catch (JSONException e) {
            Log.d(TAG, "nstreams not set.");
        }
        try {
            String direction = jsonObject.getString(DIRECTION);
            Log.d(TAG, "Iperf3Parameter: direction: "+direction);
            this.direction = Iperf3Direction.valueOf(direction.toUpperCase().trim());
        } catch (JSONException e) {
            this.direction = Iperf3Direction.UP;
            Log.d(TAG, "direction not set.");
        }
        try {
            this.oneOff = jsonObject.getBoolean(ONEOFF);
        } catch (JSONException e) {
            Log.d(TAG, "oneOff not set.");
        }
        try {
            this.protocol = Iperf3Protocol.valueOf(jsonObject.getString(PROTOCOL).toUpperCase().trim());
        } catch (JSONException | IllegalArgumentException e) {
            Log.d(TAG, "protocol not set."+e.getMessage());
            Log.e(TAG, "Iperf3Parameter: No matching Protocol found! Using TCP as default.");
            this.protocol = Iperf3Protocol.TCP;
        }
        try {
            boolean isServer = false, isClient = false;
            try {
                // check if server
                isServer = jsonObject.getBoolean(SERVER);
            } catch (JSONException e) {
                Log.d(TAG, "mode not set.");
            }
            try {
                //check if client
                 jsonObject.getString(HOST);
                 isClient = true;
            } catch (JSONException e) {
                isClient = false;
                Log.d(TAG, "mode not set.");
            }
            // check if both are set
            if(isServer && isClient){
                throw new IllegalArgumentException("Iperf3Parameter: Server and Client mode cannot be set at the same time.");
            }
            // check if none is set
            if(isServer){
                this.mode = Iperf3Mode.SERVER;
            } else if(isClient){
                this.mode = Iperf3Mode.CLIENT;
            } else {
                throw new IllegalArgumentException("Iperf3Parameter: Server or Client mode must be set.");
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "mode not set.");
            Log.e(TAG, e.getMessage());
        }
        try {
        this.cport = jsonObject.getInt(CPORT);
        } catch (JSONException e) {
            Log.d(TAG, "cport not set.");
        }

        try{
            this.username = jsonObject.getString(USERNAME);
        } catch (JSONException e) {
            Log.d(TAG, "username not set.");
        }
        try{
            this.rsaPublicKeyPath = jsonObject.getString(RSAPUBLICKEYPATH);
        } catch (JSONException e) {
            Log.d(TAG, "rsaPublicKeyPath not set.");
        }
        try{
            this.extraData = jsonObject.getString(EXTRADATA);
        } catch (JSONException e) {
            Log.d(TAG, "extraData not set.");
        }
        try{
            this.title = jsonObject.getString(TITLE);
        } catch (JSONException e) {
            Log.d(TAG, "title not set.");
        }
        try{
            this.omit = jsonObject.getInt(OMIT);
        } catch (JSONException e) {
            Log.d(TAG, "omit not set.");
        }
        try{
            this.zerocopy = jsonObject.getBoolean(ZEROCOPY);
        } catch (JSONException e) {
            Log.d(TAG, "zerocopy not set.");
        }
        try{
            this.flowlabel = jsonObject.getInt(FLOWLABEL);
        } catch (JSONException e) {
            Log.d(TAG, "flowlabel not set.");
        }
        try{
            this.dscp = jsonObject.getString(DSCP);
        } catch (JSONException e) {
            Log.d(TAG, "dscp not set.");
        }
        try{
            this.tos = jsonObject.getInt(TOS);
        } catch (JSONException e) {
            Log.d(TAG, "tos not set.");
        }
        try{
            this.version6 = jsonObject.getBoolean(VERSION6);
        } catch (JSONException e) {
            Log.d(TAG, "version6 not set.");
        }
        try{
            this.version4 = jsonObject.getBoolean(VERSION4);
        } catch (JSONException e) {
            Log.d(TAG, "version4 not set.");
        }
        try{
            this.noDelay = jsonObject.getBoolean(NODELAY);
        } catch (JSONException e) {
            Log.d(TAG, "noDelay not set.");
        }
        try{
            this.setMss = jsonObject.getInt(SETMSS);
        } catch (JSONException e) {
            Log.d(TAG, "setMss not set.");
        }
        try{
            this.congestion = jsonObject.getString(CONGESTION);
        } catch (JSONException e) {
            Log.d(TAG, "congestion not set.");
        }
        try{
            this.window = jsonObject.getString(WINDOW);
        } catch (JSONException e) {
            Log.d(TAG, "window not set.");
        }

        try{
            this.parallel = jsonObject.getInt(PARALLEL);
        } catch (JSONException e) {
            Log.d(TAG, "parallel not set.");
        }
        try{
            this.blockcount = jsonObject.getString(BLOCKCOUNT);
        } catch (JSONException e) {
            Log.d(TAG, "blockcount not set.");
        }
        try{
            this.bytes = jsonObject.getString(BYTES);
        } catch (JSONException e) {
            Log.d(TAG, "bytes not set.");
        }
        try{
            this.fqRate = jsonObject.getString(FQRATE);
        } catch (JSONException e) {
            Log.d(TAG, "fqRate not set.");
        }
        try{
            this.pacingTimer = jsonObject.getString(PACINGTIMER);
        } catch (JSONException e) {
            Log.d(TAG, "pacingTimer not set.");
        }
        try{
            this.connectTimeout = jsonObject.getInt(CONNECTTIMEOUT);
        } catch (JSONException e) {
            Log.d(TAG, "connectTimeout not set.");
        }
        try{
            this.xbind = jsonObject.getString(XBIND);
        } catch (JSONException e) {
            Log.d(TAG, "xbind not set.");
        }
        try{
            this.sctp = jsonObject.getBoolean(SCTP);
        } catch (JSONException e) {
            Log.d(TAG, "sctp not set.");
        }
        try{
            this.usePkcs1Padding = jsonObject.getBoolean(USEPKCS1PADDING);
        } catch (JSONException e) {
            Log.d(TAG, "usePkcs1Padding not set.");
        }
        try{
            this.timeSkewThreshold = jsonObject.getInt(TIMESKEWTHRESHOLD);
        } catch (JSONException e) {
            Log.d(TAG, "timeSkewThreshold not set.");
        }
        try{
            this.authorizedUsersPath = jsonObject.getString(AUTHORIZEDUSERSPATH);
        } catch (JSONException e) {
            Log.d(TAG, "authorizedUsersPath not set.");
        }
        try{
            this.rsaPrivateKeyPath = jsonObject.getString(RSAPRIVATEKEYPATH);
        } catch (JSONException e) {
            Log.d(TAG, "rsaPrivateKeyPath not set.");
        }
        try{
            this.idleTimeout = jsonObject.getInt(IDLETIMEOUT);
        } catch (JSONException e) {
            Log.d(TAG, "idleTimeout not set.");
        }
        try{
            this.serverBitrateLimit = jsonObject.getString(SERVERBITRATELIMIT);
        } catch (JSONException e) {
            Log.d(TAG, "serverBitrateLimit not set.");
        }
        try{
            this.oneOff = jsonObject.getBoolean(ONEOFF);
        } catch (JSONException e) {
            Log.d(TAG, "oneOff not set.");
        }
        try{
            this.daemon = jsonObject.getBoolean(DAEMON);
        } catch (JSONException e) {
            Log.d(TAG, "daemon not set.");
        }
        try{
            this.help = jsonObject.getBoolean(HELP);
        } catch (JSONException e) {
            Log.d(TAG, "help not set.");
        }
        try{
            this.version = jsonObject.getBoolean(VERSION);
        } catch (JSONException e) {
            Log.d(TAG, "version not set.");
        }
        try{
            this.debug = jsonObject.getInt(DEBUG);
        } catch (JSONException e) {
            Log.d(TAG, "debug not set.");
        }
        try{
            this.sndTimeout = jsonObject.getInt(SNDTIMEOUT);
        } catch (JSONException e) {
            Log.d(TAG, "sndTimeout not set.");
        }
        try{
            this.rcvTimeout = jsonObject.getInt(RCVTIMEOUT);
        } catch (JSONException e) {
            Log.d(TAG, "rcvTimeout not set.");
        }
        try{
            this.timestamps = jsonObject.getString(TIMESTAMPS);
        } catch (JSONException e) {
            Log.d(TAG, "timestamps not set.");
        }
        try{
            this.forceflush = jsonObject.getBoolean(FORCEFLUSH);
        } catch (JSONException e) {
            Log.d(TAG, "forceflush not set.");
        }

        try{
            this.verbose = jsonObject.getBoolean(VERBOSE);
        } catch (JSONException e) {
            Log.d(TAG, "verbose not set.");
        }
        try{
            this.bindDev = jsonObject.getString(BINDDEV);
        } catch (JSONException e) {
            Log.d(TAG, "bindDev not set.");
        }
        try{
            this.affinity = jsonObject.getString(AFFINITY);
        } catch (JSONException e) {
            Log.d(TAG, "affinity not set.");
        }
        try{
            this.file = jsonObject.getString(FILE);
        } catch (JSONException e) {
            Log.d(TAG, "file not set.");
        }
        try {
            String mode = jsonObject.getString(MODE);
            Log.d(TAG, "Iperf3Parameter: mode: "+mode);
            this.mode = Iperf3Mode.valueOf(mode.toUpperCase().trim());
        } catch (JSONException e) {
            Log.d(TAG, "mode not set.");
            Log.i(TAG, "Iperf3Parameter: No mode set. Defaulting to Client.");
            this.mode = Iperf3Mode.CLIENT;
        }
        try{
            this.bind = jsonObject.getString(BIND);
        } catch (JSONException e) {
            Log.d(TAG, "bind not set.");
        }
        try{
            this.title = jsonObject.getString(TITLE);
        } catch (JSONException e) {
            Log.d(TAG, "title not set.");
        }
        try {
            Files.createDirectories(Paths.get(rawDirPath));
            Files.createDirectories(Paths.get(lineProtocolDirPath));
        } catch (IOException e) {
            Log.d(TAG, "Could not create directories.");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(host);
        dest.writeString(iPerf3UUID);
        dest.writeInt(port);
        dest.writeDouble(interval);
        dest.writeString(bitrate);
        dest.writeInt(length);
        dest.writeString(mode.name());
        dest.writeString(direction.name());
        dest.writeString(pidfile);
        dest.writeString(file);
        dest.writeString(affinity);
        dest.writeString(bind);
        dest.writeString(bindDev);
        dest.writeBoolean(verbose);
        dest.writeBoolean(json);
        dest.writeBoolean(jsonStream);
        dest.writeBoolean(forceflush);
        dest.writeString(timestamps);
        dest.writeInt(rcvTimeout);
        dest.writeInt(sndTimeout);
        dest.writeInt(debug);
        dest.writeBoolean(version);
        dest.writeBoolean(help);
        dest.writeBoolean(daemon);
        dest.writeBoolean(oneOff);
        dest.writeString(serverBitrateLimit);
        dest.writeInt(idleTimeout);
        dest.writeString(rsaPrivateKeyPath);
        dest.writeString(authorizedUsersPath);
        dest.writeInt(timeSkewThreshold);
        dest.writeBoolean(usePkcs1Padding);
        dest.writeBoolean(sctp);
        dest.writeString(xbind);
        dest.writeInt(nstreams);
        dest.writeInt(connectTimeout);
        dest.writeString(pacingTimer);
        dest.writeString(fqRate);
        dest.writeInt(time);
        dest.writeString(bytes);
        dest.writeString(blockcount);
        dest.writeInt(cport);
        dest.writeInt(parallel);
        dest.writeBoolean(reverse);
        dest.writeBoolean(bidir);
        dest.writeString(window);
        dest.writeString(congestion);
        dest.writeInt(setMss);
        dest.writeBoolean(noDelay);
        dest.writeBoolean(version4);
        dest.writeBoolean(version6);
        dest.writeInt(tos);
        dest.writeString(dscp);
        dest.writeInt(flowlabel);
        dest.writeBoolean(zerocopy);
        dest.writeInt(omit);
        dest.writeString(title);
        dest.writeString(extraData);
        dest.writeBoolean(getServerOutput);
        dest.writeBoolean(udpCounters64bit);
        dest.writeBoolean(repeatingPayload);
        dest.writeBoolean(dontFragment);
        dest.writeString(username);
        dest.writeString(rsaPublicKeyPath);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String s) {
        this.host = s;
    }

    public void setBandwidth(String s) {
        this.bitrate = s ;
    }


    // --- Enums ---
    public enum Iperf3Mode {
        CLIENT,
        SERVER,
        UNDEFINED;
        public String toPrettyPrint() {
            return this.name().substring(0, 1).toUpperCase() + this.name().toLowerCase().substring(1);
        }
    }

    public enum Iperf3Protocol {
        TCP,
        UDP,
        UNDEFINED;
    }

    public enum Iperf3Direction {
        UP,
        DOWN,
        BIDIR,
        UNDEFINED;
        public String toPrettyPrint() {
            return this.name().toLowerCase();
        }
    }

    // --- Fields ---
    // Required field.
    private String host;
    private String iPerf3UUID;
    private String testUUID;
    // Optional fields with defaults based on iperf3.
    private Iperf3Protocol protocol = Iperf3Protocol.TCP;  // Default protocol: TCP.
    private int port = 5201;                     // Default port: 5201.
    private double interval = 1.0;               // Default interval: 1.0 second.
    // For UDP mode: if not set, default bitrate is "1M".
    private String bitrate;
    // For TCP mode: if not set, default buffer length is 131072 (128 KB).
    private int length;

    // Mode selection fields.
    // The mode field indicates whether the test is run in CLIENT or SERVER mode.
    // When mode is CLIENT, the client field (host) must be provided.
    private Iperf3Mode mode = Iperf3Mode.UNDEFINED;


    private Iperf3Direction direction = Iperf3Direction.UNDEFINED;

    // Additional optional parameters.
    private String pidfile;
    private String file;
    private String affinity;
    private String bind;
    private String bindDev;
    private Boolean verbose;
    private Boolean json;
    private Boolean jsonStream = true;
    private Boolean forceflush;
    private String timestamps;
    private Integer rcvTimeout;
    private Integer sndTimeout;
    private Integer debug;
    private Boolean version;
    private Boolean help;
    private Boolean daemon;
    private Boolean oneOff;
    private String serverBitrateLimit;
    private Integer idleTimeout;
    private String rsaPrivateKeyPath;
    private String authorizedUsersPath;
    private Integer timeSkewThreshold;
    private Boolean usePkcs1Padding;
    private Boolean sctp;
    private String xbind;
    private Integer nstreams;

    private Integer connectTimeout;
    private String pacingTimer;
    private String fqRate;
    private Integer time = 10;
    private String bytes;
    private String blockcount;
    private Integer cport;
    private Integer parallel;
    private Boolean reverse;
    private Boolean bidir;
    private String window;
    private String congestion;
    private Integer setMss;
    private Boolean noDelay;
    private Boolean version4;
    private Boolean version6;
    private Integer tos;
    private String dscp;
    private Integer flowlabel;
    private Boolean zerocopy;
    private Integer omit;
    private String title;
    private String extraData;
    private Boolean getServerOutput;
    private Boolean udpCounters64bit;
    private Boolean repeatingPayload;
    private Boolean dontFragment;
    private String username;
    private String rsaPublicKeyPath;


    // --- Getters and Setters ---

    public String getiPerf3UUID() {
        return iPerf3UUID;
    }

    public void setiPerf3UUID(String iPerf3UUID) {
        this.iPerf3UUID = iPerf3UUID;
    }

    public Iperf3Protocol getProtocol() {
        return protocol;
    }

    /**
     * Set the protocol. If the input is not null, the protocol is set accordingly.
     * Otherwise, it defaults to TCP.
     */
    public void setProtocol(Iperf3Protocol protocol) {
        this.protocol = (protocol != null) ? protocol : Iperf3Protocol.TCP;
    }
    public void setTestUUID(String testUUID) {
        this.testUUID = testUUID;
    }
    public int getPort() {
        if(port == 0){
            return 5201;
        }
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getInterval() {
        if(interval == 0.0){
            return 1.0;
        }

        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Iperf3Mode getMode() {
        return mode;
    }

    /**
     * Set the mode. If the mode is set to SERVER, the client field is cleared.
     */
    public void setMode(Iperf3Mode mode) {
        this.mode = (mode != null) ? mode : Iperf3Mode.UNDEFINED;
    }



    public Iperf3Direction getDirection() {
        return direction;
    }

    public void setDirection(Iperf3Direction direction) {
        this.direction = (direction != null) ? direction : Iperf3Direction.UNDEFINED;
    }

    // Additional getters and setters for remaining fields...

    public String getPidfile() {
        return pidfile;
    }

    public void setPidfile(String pidfile) {
        this.pidfile = pidfile;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public String getBindDev() {
        return bindDev;
    }

    public void setBindDev(String bindDev) {
        this.bindDev = bindDev;
    }

    public Boolean getVerbose() {
        return verbose;
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public Boolean getJson() {
        return json;
    }

    public void setJson(Boolean json) {
        this.json = json;
    }

    public Boolean getJsonStream() {
        return jsonStream;
    }

    public void setJsonStream(Boolean jsonStream) {
        this.jsonStream = jsonStream;
    }


    public Boolean getForceflush() {
        return forceflush;
    }

    public void setForceflush(Boolean forceflush) {
        this.forceflush = forceflush;
    }

    public String getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(String timestamps) {
        this.timestamps = timestamps;
    }

    public Integer getRcvTimeout() {
        return rcvTimeout;
    }

    public void setRcvTimeout(Integer rcvTimeout) {
        this.rcvTimeout = rcvTimeout;
    }

    public Integer getSndTimeout() {
        return sndTimeout;
    }

    public void setSndTimeout(Integer sndTimeout) {
        this.sndTimeout = sndTimeout;
    }

    public Integer getDebug() {
        return debug;
    }

    public void setDebug(Integer debug) {
        this.debug = debug;
    }

    public Boolean getVersion() {
        return version;
    }

    public void setVersion(Boolean version) {
        this.version = version;
    }

    public Boolean getHelp() {
        return help;
    }

    public void setHelp(Boolean help) {
        this.help = help;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(Boolean daemon) {
        this.daemon = daemon;
    }

    public Boolean getOneOff() {
        return oneOff;
    }

    public void setOneOff(Boolean oneOff) {
        this.oneOff = oneOff;
    }

    public String getServerBitrateLimit() {
        return serverBitrateLimit;
    }

    public void setServerBitrateLimit(String serverBitrateLimit) {
        this.serverBitrateLimit = serverBitrateLimit;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getRsaPrivateKeyPath() {
        return rsaPrivateKeyPath;
    }

    public void setRsaPrivateKeyPath(String rsaPrivateKeyPath) {
        this.rsaPrivateKeyPath = rsaPrivateKeyPath;
    }

    public String getAuthorizedUsersPath() {
        return authorizedUsersPath;
    }

    public void setAuthorizedUsersPath(String authorizedUsersPath) {
        this.authorizedUsersPath = authorizedUsersPath;
    }

    public Integer getTimeSkewThreshold() {
        return timeSkewThreshold;
    }

    public void setTimeSkewThreshold(Integer timeSkewThreshold) {
        this.timeSkewThreshold = timeSkewThreshold;
    }

    public Boolean getUsePkcs1Padding() {
        return usePkcs1Padding;
    }

    public void setUsePkcs1Padding(Boolean usePkcs1Padding) {
        this.usePkcs1Padding = usePkcs1Padding;
    }

    public Boolean getSctp() {
        return sctp;
    }

    public void setSctp(Boolean sctp) {
        this.sctp = sctp;
    }

    public String getXbind() {
        return xbind;
    }

    public void setXbind(String xbind) {
        this.xbind = xbind;
    }

    public Integer getNstreams() {
        return nstreams;
    }

    public void setNstreams(Integer nstreams) {
        this.nstreams = nstreams;
    }


    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getPacingTimer() {
        return pacingTimer;
    }

    public void setPacingTimer(String pacingTimer) {
        this.pacingTimer = pacingTimer;
    }

    public String getFqRate() {
        return fqRate;
    }

    public void setFqRate(String fqRate) {
        this.fqRate = fqRate;
    }

    public Integer getTime() {
        if(time == 0){
            time = 10;
        }
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public String getBlockcount() {
        return blockcount;
    }

    public void setBlockcount(String blockcount) {
        this.blockcount = blockcount;
    }

    public Integer getCport() {
        return cport;
    }

    public void setCport(Integer cport) {
        this.cport = cport;
    }

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public Boolean getReverse() {
        return reverse;
    }

    public void setReverse(Boolean reverse) {
        this.reverse = reverse;
    }

    public Boolean getBidir() {
        return bidir;
    }

    public void setBidir(Boolean bidir) {
        this.bidir = bidir;
    }

    public String getWindow() {
        return window;
    }

    public void setWindow(String window) {
        this.window = window;
    }

    public String getCongestion() {
        return congestion;
    }

    public void setCongestion(String congestion) {
        this.congestion = congestion;
    }

    public Integer getSetMss() {
        return setMss;
    }

    public void setSetMss(Integer setMss) {
        this.setMss = setMss;
    }

    public Boolean getNoDelay() {
        return noDelay;
    }

    public void setNoDelay(Boolean noDelay) {
        this.noDelay = noDelay;
    }

    public Boolean getVersion4() {
        return version4;
    }

    public void setVersion4(Boolean version4) {
        this.version4 = version4;
    }

    public Boolean getVersion6() {
        return version6;
    }

    public void setVersion6(Boolean version6) {
        this.version6 = version6;
    }

    public Integer getTos() {
        return tos;
    }

    public void setTos(Integer tos) {
        this.tos = tos;
    }

    public String getDscp() {
        return dscp;
    }

    public void setDscp(String dscp) {
        this.dscp = dscp;
    }

    public Integer getFlowlabel() {
        return flowlabel;
    }

    public void setFlowlabel(Integer flowlabel) {
        this.flowlabel = flowlabel;
    }

    public Boolean getZerocopy() {
        return zerocopy;
    }

    public void setZerocopy(Boolean zerocopy) {
        this.zerocopy = zerocopy;
    }

    public Integer getOmit() {
        return omit;
    }

    public void setOmit(Integer omit) {
        this.omit = omit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public Boolean getGetServerOutput() {
        return getServerOutput;
    }

    public void setGetServerOutput(Boolean getServerOutput) {
        this.getServerOutput = getServerOutput;
    }

    public Boolean getUdpCounters64bit() {
        return udpCounters64bit;
    }

    public void setUdpCounters64bit(Boolean udpCounters64bit) {
        this.udpCounters64bit = udpCounters64bit;
    }

    public Boolean getRepeatingPayload() {
        return repeatingPayload;
    }

    public void setRepeatingPayload(Boolean repeatingPayload) {
        this.repeatingPayload = repeatingPayload;
    }

    public Boolean getDontFragment() {
        return dontFragment;
    }

    public void setDontFragment(Boolean dontFragment) {
        this.dontFragment = dontFragment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRsaPublicKeyPath() {
        return rsaPublicKeyPath;
    }

    public void setRsaPublicKeyPath(String rsaPublicKeyPath) {
        this.rsaPublicKeyPath = rsaPublicKeyPath;
    }

    @Override
    public String toString() {
        return "Params{" +
                "iPerf3UUID='" + iPerf3UUID + '\'' +
                ", protocol=" + protocol +
                ", port=" + port +
                ", interval=" + interval +
                ", bitrate='" + bitrate + '\'' +
                ", length=" + length +
                ", mode=" + mode +
                ", direction=" + direction +
                ", pidfile='" + pidfile + '\'' +
                ", file='" + file + '\'' +
                ", affinity='" + affinity + '\'' +
                ", bind='" + bind + '\'' +
                ", bindDev='" + bindDev + '\'' +
                ", verbose=" + verbose +
                ", json=" + json +
                ", jsonStream=" + jsonStream +
                ", forceflush=" + forceflush +
                ", timestamps='" + timestamps + '\'' +
                ", rcvTimeout=" + rcvTimeout +
                ", sndTimeout=" + sndTimeout +
                ", debug=" + debug +
                ", version=" + version +
                ", help=" + help +
                ", daemon=" + daemon +
                ", oneOff=" + oneOff +
                ", serverBitrateLimit='" + serverBitrateLimit + '\'' +
                ", idleTimeout=" + idleTimeout +
                ", rsaPrivateKeyPath='" + rsaPrivateKeyPath + '\'' +
                ", authorizedUsersPath='" + authorizedUsersPath + '\'' +
                ", timeSkewThreshold=" + timeSkewThreshold +
                ", usePkcs1Padding=" + usePkcs1Padding +
                ", sctp=" + sctp +
                ", xbind='" + xbind + '\'' +
                ", nstreams=" + nstreams +
                ", connectTimeout=" + connectTimeout +
                ", pacingTimer='" + pacingTimer + '\'' +
                ", fqRate='" + fqRate + '\'' +
                ", time=" + time +
                ", bytes='" + bytes + '\'' +
                ", blockcount='" + blockcount + '\'' +
                ", cport=" + cport +
                ", parallel=" + parallel +
                ", reverse=" + reverse +
                ", bidir=" + bidir +
                ", window='" + window + '\'' +
                ", congestion='" + congestion + '\'' +
                ", setMss=" + setMss +
                ", noDelay=" + noDelay +
                ", version4=" + version4 +
                ", version6=" + version6 +
                ", tos=" + tos +
                ", dscp='" + dscp + '\'' +
                ", flowlabel=" + flowlabel +
                ", zerocopy=" + zerocopy +
                ", omit=" + omit +
                ", title='" + title + '\'' +
                ", extraData='" + extraData + '\'' +
                ", getServerOutput=" + getServerOutput +
                ", udpCounters64bit=" + udpCounters64bit +
                ", repeatingPayload=" + repeatingPayload +
                ", dontFragment=" + dontFragment +
                ", username='" + username + '\'' +
                ", rsaPublicKeyPath='" + rsaPublicKeyPath + '\'' +
                '}';
    }

    public String[] getInputAsCommand() {
        ArrayList<String> command = new ArrayList<>();

        // Mode: either CLIENT (-c host) or SERVER (-s)
        switch (mode) {
            case CLIENT:
                command.add("-c");
                command.add(host);
                break;
            case SERVER:
                command.add("-s");
                break;
            default:
                // Optionally, throw an exception or handle UNDEFINED mode
                break;
        }

        // Common options
        if (port > 0) {
            command.add("-p");
            command.add(String.valueOf(port));
        }
        if (interval > 0) {
            command.add("-i");
            command.add(String.valueOf(interval));
        }
        if (pidfile != null && !pidfile.trim().isEmpty()) {
            command.add("-I");
            command.add(pidfile);
        }
        if (file != null && !file.trim().isEmpty()) {
            command.add("-F");
            command.add(file);
        }
        if (affinity != null && !affinity.trim().isEmpty()) {
            command.add("-A");
            command.add(affinity);
        }
        if (bind != null && !bind.trim().isEmpty()) {
            command.add("-B");
            command.add(bind);
        }
        if (bindDev != null && !bindDev.trim().isEmpty()) {
            command.add("--bind-dev");
            command.add(bindDev);
        }
        if (verbose != null && verbose) {
            command.add("-V");
        }
        if (json != null && json) {
            command.add("-J");
        }

        if (timestamps != null && !timestamps.trim().isEmpty()) {
            command.add("--timestamps");
            command.add(timestamps);
        }
        if (rcvTimeout != null && rcvTimeout > 0) {
            command.add("--rcv-timeout");
            command.add(String.valueOf(rcvTimeout));
        }
        if (sndTimeout != null && sndTimeout > 0) {
            command.add("--snd-timeout");
            command.add(String.valueOf(sndTimeout));
        }
        if (debug != null && debug > 0) {
            command.add("-d");
            command.add(String.valueOf(debug));
        }
        if (version != null && version) {
            command.add("-v");
        }
        if (help != null && help) {
            command.add("-h");
        }

        // Server-specific options
        if (daemon != null && daemon) {
            command.add("-D");
        }
        if (oneOff != null && oneOff) {
            command.add("-1");
        }
        if (serverBitrateLimit != null && !serverBitrateLimit.trim().isEmpty()) {
            command.add("--server-bitrate-limit");
            command.add(serverBitrateLimit);
        }
        if (idleTimeout != null && idleTimeout > 0) {
            command.add("--idle-timeout");
            command.add(String.valueOf(idleTimeout));
        }
        if (rsaPrivateKeyPath != null && !rsaPrivateKeyPath.trim().isEmpty()) {
            command.add("--rsa-private-key-path");
            command.add(rsaPrivateKeyPath);
        }
        if (authorizedUsersPath != null && !authorizedUsersPath.trim().isEmpty()) {
            command.add("--authorized-users-path");
            command.add(authorizedUsersPath);
        }
        if (timeSkewThreshold != null && timeSkewThreshold > 0) {
            command.add("--time-skew-threshold");
            command.add(String.valueOf(timeSkewThreshold));
        }
        if (usePkcs1Padding != null && usePkcs1Padding) {
            command.add("--use-pkcs1-padding");
        }

        // Client-specific options
        if (sctp != null && sctp) {
            command.add("--sctp");
        }
        if (xbind != null && !xbind.trim().isEmpty()) {
            command.add("-X");
            command.add(xbind);
        }
        if (nstreams != null && nstreams > 0) {
            command.add("--nstreams");
            command.add(String.valueOf(nstreams));
        }
        switch (protocol){
            case UDP:
                command.add("-u");
                break;
            case TCP:
            default:
                break;
        }
        if (connectTimeout != null && connectTimeout > 0) {
            command.add("--connect-timeout");
            command.add(String.valueOf(connectTimeout));
        }
        if (bitrate != null && !bitrate.trim().isEmpty()) {
            command.add("-b");
            command.add(bitrate+"M");
        }
        if (pacingTimer != null && !pacingTimer.trim().isEmpty()) {
            command.add("--pacing-timer");
            command.add(pacingTimer);
        }
        if (fqRate != null && !fqRate.trim().isEmpty()) {
            command.add("--fq-rate");
            command.add(fqRate);
        }
        if (time > 0) {
            command.add("-t");
            command.add(String.valueOf(time));
        }
        if (bytes != null && !bytes.trim().isEmpty()) {
            command.add("-n");
            command.add(bytes);
        }
        if (blockcount != null && !blockcount.trim().isEmpty()) {
            command.add("-k");
            command.add(blockcount);
        }
        if (length > 0) {
            command.add("-l");
            command.add(String.valueOf(length));
        }
        if (cport != null && cport > 0) {
            command.add("--cport");
            command.add(String.valueOf(cport));
        }
        if (parallel != null && parallel > 0) {
            command.add("-P");
            command.add(String.valueOf(parallel));
        }

        // Data direction options (client-specific)
        if (direction != null) {
            switch (direction) {
                case DOWN:
                    command.add("--reverse");
                    break;
                case BIDIR:
                    command.add("--bidir");
                    break;
                default:
                    // UP direction requires no flag.
                    break;
            }
        }

        if (window != null && !window.trim().isEmpty()) {
            command.add("-w");
            command.add(window);
        }
        if (congestion != null && !congestion.trim().isEmpty()) {
            command.add("-C");
            command.add(congestion);
        }
        if (setMss != null && setMss > 0) {
            command.add("-M");
            command.add(String.valueOf(setMss));
        }
        if (noDelay != null && noDelay) {
            command.add("-N");
        }
        if (version4 != null && version4) {
            command.add("-4");
        }
        if (version6 != null && version6) {
            command.add("-6");
        }
        if (tos != null && tos > 0) {
            command.add("-S");
            command.add(String.valueOf(tos));
        }
        if (dscp != null && !dscp.trim().isEmpty()) {
            command.add("--dscp");
            command.add(dscp);
        }
        if (flowlabel != null && flowlabel > 0) {
            command.add("-L");
            command.add(String.valueOf(flowlabel));
        }
        if (zerocopy != null && zerocopy) {
            command.add("-Z");
        }
        if (omit != null && omit > 0) {
            command.add("-O");
            command.add(String.valueOf(omit));
        }
        if (title != null && !title.trim().isEmpty()) {
            command.add("-T");
            command.add(title);
        }
        if (extraData != null && !extraData.trim().isEmpty()) {
            command.add("--extra-data");
            command.add(extraData);
        }
        if (getServerOutput != null && getServerOutput) {
            command.add("--get-server-output");
        }
        if (udpCounters64bit != null && udpCounters64bit) {
            command.add("--udp-counters-64bit");
        }
        if (repeatingPayload != null && repeatingPayload) {
            command.add("--repeating-payload");
        }
        if (dontFragment != null && dontFragment) {
            command.add("--dont-fragment");
        }
        if (username != null && !username.trim().isEmpty()) {
            command.add("--username");
            command.add(username);
        }
        if (rsaPublicKeyPath != null && !rsaPublicKeyPath.trim().isEmpty()) {
            command.add("--rsa-public-key-path");
            command.add(rsaPublicKeyPath);
        }

        // Always add these extra fixed options.
        command.add("--json-stream");
        command.add("--forceflush");
        command.add("--logfile");
        command.add(super.getLogfile());



        return command.toArray(new String[0]);
    }

}
