package me.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import me.jackson.utlis.ConfigReader;
import me.jackson.yamls.DailyConfig;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/21
 */
public class Main {
    static String path = "";
    static String privateKeyGit = "";
    static String repoUrl = "";
    static String repoBranch = "";
    static String repoRemote = "";
    static Integer dailyMaximumCommit = 0;
    static String filename = "";


    static {
        path = ConfigReader.read("repo.savePath");
        privateKeyGit = ConfigReader.read("privateKeyDir");
        repoUrl = ConfigReader.read("repo.url");
        repoBranch = ConfigReader.read("repo.branch");
        repoRemote = ConfigReader.read("repo.remote");
        dailyMaximumCommit = Integer.parseInt(ConfigReader.read("general.dailyMaximumCommit"));
        filename = ConfigReader.read("general.filename");
    }

    public static void main(String[] args) throws GitAPIException, IOException, URISyntaxException {
        File repoPath = new File(path);
        Git git = null;
        DailyConfig config = null;
        if (!repoPath.exists()) {
            CloneCommand cloneCommand = Git.cloneRepository();
            // TODO Config
            cloneCommand
                    .setURI(repoUrl)
                    .setBranch(repoBranch)
                    .setRemote(repoRemote)
                    .setTimeout(200)
                    .setDirectory(new File(path));
            git = cloneCommand.setTransportConfigCallback(transportConfigCallback).call();
            config = new DailyConfig(Date.from(Instant.now()));
            mapper.writeValue(new File(path + "/temp.yaml"), config);

            git.add().addFilepattern(".").call();
            git.commit().setMessage("updating").call();
            PushCommand pushCommand = git.push();
            pushCommand.setTransportConfigCallback(transportConfigCallback).call();
            System.out.println("Initialize file... first time committed!");

        } else {
            RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
            // TODO optimization here
            repositoryBuilder.setGitDir(new File(path + "/.git"));
            git = new Git(repositoryBuilder.build());
        }
        // Schedule
        final Git finalGit = git;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    dailySchedule(path, finalGit);
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            private void dailySchedule(String path, Git git) throws GitAPIException, JsonProcessingException {
                DailyConfig config = null;
                try {
                    config = mapper.readValue(new File(path + "/temp.yaml"), DailyConfig.class);
                } catch (JsonProcessingException e) {
                    // exception here
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assert config != null;
                Long lastUpdate = config.getLastUpdate().getTime() / 1000;
                Date currDate = Date.from(Instant.now());
                Long currTime = currDate.toInstant().getLong(ChronoField.INSTANT_SECONDS);
                double diff = (currTime - lastUpdate) / 3600.0;
                // check if it's over 24 hrs
                int randTime = 0;
                if (diff > 24) {
                    Random rnd = new Random();
                    randTime = rnd.nextInt(5) + 1;
                    int i = 0;
                    while (i < randTime) {
                        config.setLastUpdate(currDate);
                        try {
                            mapper.writeValue(new File(path + "/temp.yaml"), config);
                        } catch (IOException e) {
                            // TODO add log later
                            e.printStackTrace();
                            System.out.println("something wrong in daily schedule");
                        }
                        git.add().addFilepattern(".").call();
                        git.commit().setMessage("updating").call();
                        PushCommand pushCommand = git.push();
                        pushCommand.setTransportConfigCallback(transportConfigCallback).call();
                        i++;
                    }
                }
                System.out.println("running " + randTime + " times");
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 86400000);
    }

    /**
     * Help make ssh session
     */
    static SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
            session.setConfig("StrictHostKeyChecking", "yes");
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            // TODO config
            defaultJSch.addIdentity(privateKeyGit);
            return defaultJSch;
        }
    };

    static TransportConfigCallback transportConfigCallback = new TransportConfigCallback() {
        @Override
        public void configure(Transport transport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }

    };

    static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());


}
