package me.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import me.jackson.yamls.DailyConfig;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/21
 */
public class Main {
    static String path = "./path/to/repo";

    public static void main(String[] args) throws GitAPIException, IOException, URISyntaxException {
        // Git git = Git.cloneRepository()
        //         .setCredentialsProvider(new UsernamePasswordCredentialsProvider("Sma1lboy", "Aa20021001"))
        //         .setURI("https://github.com/Sma1lboy/DailyCommitJava.git")
        //         .setDirectory(new File(path))
        //         .call();
        // Repository repository = git.getRepository();
        // CommitCommand commit = git.commit();
        // commit.setMessage("trying to commit from bot").call();
        // git.remoteAdd().setName("origin").setUri(new URIish("https://github.com/Sma1lboy/DailyCommitJava.git"));
        // git.push().call();


        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "yes");

            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                defaultJSch.addIdentity("/Users/jacksonchen/.ssh/id_ecdsa");
                return defaultJSch;
            }
        };
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand
                .setURI("git@github.com:Sma1lboy/DailyCommitJava.git")
                .setBranch("main")
                .setRemote("origin")
                .setTimeout(200)

        ;
        cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
            @Override
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }

        }).call();



        /*
        initial update file
         */
        // DailyConfig config = new DailyConfig(Date.from(Instant.now()));
        // ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        // mapper.writeValue(new File(path + "/temp.yaml"), config);
    }

    private void dailySchedule(ObjectMapper mapper, DailyConfig config, File path) {
        Long lastUpdate = config.getLastUpdate().toInstant().getLong(ChronoField.INSTANT_SECONDS);
        Date currDate = Date.from(Instant.now());
        Long currTime = currDate.toInstant().getLong(ChronoField.INSTANT_SECONDS);
        double diff = (currTime - lastUpdate) / 3600.0;
        // check if it's over 24 hrs
        if (diff > 24) {
            config.setLastUpdate(currDate);
        }
        try {
            mapper.writeValue(path, config);
        } catch (IOException e) {
            // TODO add log later
            e.printStackTrace();
            System.out.println("something wrong in daily schedule");
        }
        // commit
    }

    private void dailyCommit() {

    }


}
