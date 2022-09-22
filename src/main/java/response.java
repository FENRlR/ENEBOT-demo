import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.*;
import java.io.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.List;


public class response extends ListenerAdapter{

    public void onMessageReceived(MessageReceivedEvent event){

        String message = event.getMessage().getContentRaw();//message

        //- For HTTPS connection
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
                    public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
                }
        };
        try {//Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance( "SSL" );
            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier(){
                        public boolean verify(String urlHostName, SSLSession session) {
                            return true;
                        }
                    });
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        //- Definition for PhantomJS(Selenium)
        String os = System.getProperty("os.name");
        if(os.startsWith("Windows"))
        {
            System.setProperty("phantomjs.binary.path", "./phantomjs/win/phantomjs.exe");
        }
        else if(os.startsWith("Mac"))
        {
            System.setProperty("phantomjs.binary.path", "./phantomjs/osx/phantomjs");
        }
        else
        {
            System.setProperty("phantomjs.binary.path", "./vendor/phantomjs/bin/phantomjs");//Linux 64
        }

        //- Instruction command
        if(message.startsWith("!ene"))
        {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.cyan);
            eb.setTitle("- Command List");
            eb.setDescription(
                    "**!ping :** a simple ping-pong command" +
                    "\n**!combatbox :** parses server data\n(arternatives : !finnish,!berloga,!wol)" +
                    "\n**!ud :** user dictionary" +
                    "\n**!sc :** schedule registration" +
                    "\n**!pk :** randomly choose one from list of objects" +
                    "\n**!random : returns a number between 1~100**"
            );
            MessageEmbed embed = eb.build();
            event.getTextChannel().sendMessage(embed).queue();
        }


        //- IL-2 SERVER STATS PARSER
        if(message.startsWith("!combatbox"))//COMBAT BOX BY RED FLIGHT
        {
            try{
                String cord = "http://combatbox.net/en/online/";
                Document doc = Jsoup.connect(cord).get();

                Elements totnum = doc.select("div.wrapper div.content_head div.content_title");//total number of players

                //number of players for each team
                Elements alnum = doc.select("div.wrapper div.online_players div.online_coal_1 div.header");
                Elements axnum = doc.select("div.wrapper div.online_players div.online_coal_2 div.header");

                cord = "http://combatbox.net";
                doc = Jsoup.connect(cord).get();

                Elements almw= doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_red_num");
                Elements axmw= doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_blue_num");

                Elements comtitle = doc.select("section#main div.wrapper div.dominant_coal a:eq(0)");

                String comtime = doc.select("div.wrapper div.dominant_coal").text().split(", ",2)[1].split(" Next",2)[0];

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.cyan);
                eb.setAuthor("IL-2 STATS", cord, "https://static.combatbox.net/apple-touch-icon-57x57.2940dcc64b95.png");
                eb.setTitle("COMBAT BOX BY RED FLIGHT");
                eb.setDescription("Current Map: "+ comtitle.text()+"\n"+ comtime + "\n**"+ totnum.text() + "**");
                eb.addField("**Allies**", "**Players Online : " + alnum.text().split(": ")[1]+ "**" + "\n" + "Won missions : " + almw.text(), true);
                eb.addField("**Axis**", "**Players Online : " + axnum.text().split(": ")[1] + "**" + "\n" + "Won missions : " + axmw.text(), true);

                String ctext = "**[" + "\nLink to mission planner" + "](" + "https://il2missionplanner.com/#combatbox" + ")**";
                eb.addField("" ,ctext,true);

                MessageEmbed embed = eb.build();
                event.getTextChannel().sendMessage(embed).queue();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                event.getTextChannel().sendMessage("Unable to parse information").queue();
            }

        }
        if(message.startsWith("!WOL")||message.startsWith("!wol"))//WINGS OF LIBERTY
        {
            try {
                String cord = "http://il2stat.aviaskins.com:8008/en/online/";

                Connection.Response response= Jsoup.connect(cord)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true)
                        .execute();

                Document doc = response.parse();

                Elements totnum = doc.select("div.wrapper div.content_head div.content_title");
                Elements alnum = doc.select("div.wrapper div.online_players div.online_coal_1 div.header");
                Elements axnum = doc.select("div.wrapper div.online_players div.online_coal_2 div.header");

                cord = "http://il2stat.aviaskins.com:8008/en/";

                response= Jsoup.connect(cord)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true)
                        .execute();

                doc = response.parse();

                Elements almw = doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_red_num");
                Elements axmw = doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_blue_num");

                String comf =  doc.select("div div div#current_mission div.total_num").text().split(" ")[0];

                String comtime = doc.select("div div div#current_mission div#duration.total_num").text();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.cyan);
                eb.setAuthor("IL-2 STATS", cord, "http://il2stat.aviaskins.com:8008/static/img/il2_stats_logo_sm.d81496c4af2d.png");
                eb.setTitle("WINGS OF LIBERTY");
                eb.setDescription("Current Map: " + comf + "\n" + comtime + " remaining" + "\n**" + totnum.text() + "**");
                eb.addField("**Allies**", "**Players Online : " + alnum.text().split(": ")[1] + "**" + "\n" + "Won missions : " + almw.text(), true);
                eb.addField("**Axis**", "**Players Online : " + axnum.text().split(": ")[1] + "**" + "\n" + "Won missions : " + axmw.text(), true);

                String ctext = "**[" + "\nLink to mission log" + "](" + "http://il2stat.aviaskins.com:8008/en/missions/" + ")**";
                eb.addField("" ,ctext,true);

                MessageEmbed embed = eb.build();
                event.getTextChannel().sendMessage(embed).queue();
            } catch (IOException e) {
                e.printStackTrace();
                event.getTextChannel().sendMessage("Unable to parse information").queue();
            }
        }
        if(message.startsWith("!finnish")||message.startsWith("!FINNISH"))//FINNISH VIRTUAL PILOTS ASSOCIATION
        {
            try{
                String cord = "";

                cord = "http://stats.virtualpilots.fi:8000/en/online/";
                Connection.Response response= Jsoup.connect(cord)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true)
                        .execute();

                Document doc = response.parse();

                Elements totnum = doc.select("div.wrapper div.content_head div.content_title");

                Elements alnum = doc.select("div.wrapper div.online_players div.online_coal_1 div.header");

                Elements axnum = doc.select("div.wrapper div.online_players div.online_coal_2 div.header");

                cord = "http://stats.virtualpilots.fi:8000/en/";
                response= Jsoup.connect(cord)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(5000)
                        .followRedirects(true)
                        .execute();

                doc = response.parse();

                Elements almw = doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_red_num");
                Elements axmw = doc.select("div.wrapper div.general_block div.dominant_bars div.bar_win_missions div.bar_blue_num");

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.cyan);
                eb.setAuthor("IL-2 STATS", cord, "http://stats.virtualpilots.fi:8000/static/apple-touch-icon-57x57.2940dcc64b95.png");
                eb.setTitle("FINNISH VIRTUAL PILOTS ASSOCIATION");
                eb.setDescription("**" + totnum.text() + "**");
                eb.addField("**Allies**", "**Players Online : " + alnum.text().split(": ")[1] + "**" + "\n" + "Won missions : " + almw.text(), true);
                eb.addField("**Axis**", "**Players Online : " + axnum.text().split(": ")[1] + "**" + "\n" + "Won missions : " + axmw.text(), true);

                String ctext = "**[" + "\nLink to mission planner" + "](" + "http://il2missionplanner.com/#virtualpilotsfi" + ")**";
                eb.addField("" ,ctext,true);

                MessageEmbed embed = eb.build();
                event.getTextChannel().sendMessage(embed).queue();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                event.getTextChannel().sendMessage("Unable to parse information").queue();
            }

        }
        if(message.startsWith("!BERLOGA")||message.startsWith("!berloga"))//BERLOGA Duel & Dogfight
        {
            try{
                String cord = "https://il2.flying-barans.ru/";
                Document doc = Jsoup.connect(cord).get();

                Elements updesc = doc.select("div.container div.panel-group div.panel.panel-default:eq(0) div.panel-body");//desc
                updesc.select("div.server_players").remove();
                String fldesc = updesc.toString().replace("<br>","").split(">",2)[1].split("<",2)[0];

                Elements totnum = doc.select("div.container div.panel-group div.panel.panel-default:eq(0) span");//players online

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.cyan);
                eb.setAuthor("IL-2 STATS", cord, "https://static.combatbox.net/apple-touch-icon-57x57.2940dcc64b95.png");
                eb.setTitle("BERLOGA Duel & Dogfight");
                eb.setDescription(fldesc + "\n**Players on the server: "+ totnum.text().split(" ",2)[0] + "**");

                MessageEmbed embed = eb.build();
                event.getTextChannel().sendMessage(embed).queue();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                event.getTextChannel().sendMessage("Unable to parse information").queue();
            }

        }

        //- Simple ping-pong response for connection test
        if(message.startsWith("!ping"))
        {
            String response = "Pong!";
            event.getTextChannel().sendMessage(response).queue();
        }

        //- Gets the information of current channel
        if(message.startsWith("!channel"))
        {
            EmbedBuilder eb = new EmbedBuilder();
            Guild guild = event.getGuild();
            Member owner = guild.getOwner();
            if(owner == null)
            {
                owner = guild.retrieveOwner(false).complete();
            }

            eb.setAuthor("current server", null, owner.getUser().getAvatarUrl());
            eb.addField("Name", guild.getName(), true);
            eb.addField("Guild ID", guild.getId(), true);
            eb.addField("Owner", owner.getEffectiveName(), true);
            eb.addField("Owner ID", owner.getUser().getId(), true);
            eb.addField("Users", guild.getMembers().size() + "", true);
            ArrayList<Member> users = new ArrayList<Member>(guild.getMembers());
            for (Member u : new ArrayList<Member>(users))//filters out bot users
            {
                if(u.getUser().isBot() || u.getUser().isFake())
                {
                    users.remove(u);
                }
            }
            eb.addField("Humans", users.size() + "", true);
            eb.addField("Bots", guild.getMembers().size() - users.size() + "", true);
            eb.addField("Channels", guild.getTextChannels().size() + "", true);

            eb.setThumbnail(guild.getIconUrl());
            MessageEmbed embed = eb.build();
            event.getTextChannel().sendMessage(embed).queue();
        }

        //- user investigation
        if(message.startsWith("!check"))
        {
            String target = "";

            try{
                target = message.split(" ")[1];
            }catch (Exception e){
                event.getTextChannel().sendMessage("Exception occurred").queue();
            }
            Guild guild = event.getGuild();
            Member tarinfo = null;
            int insw = 0;
            if(target.startsWith("<@!"))//get user by mention - TODO : currently not working due to some changes
            {
                insw = 1;
                target = target.substring(3,target.length()-1);
                tarinfo = guild.getMemberById(target);

            }
            else if(!target.startsWith("<@!"))//get user by name
            {
                insw = 1;
                tarinfo = guild.getMemberByTag(target);
            }

            if(insw==1)
            {
                event.getTextChannel().sendMessage("Effectivename : " + tarinfo.getEffectiveName()
                        + "\nNickname : " + tarinfo.getNickname()
                        + "\nTag : " + tarinfo.getUser().getAsTag()
                        + "\nAvatar : " + tarinfo.getUser().getAvatarUrl()
                        + "\nID : " + tarinfo.getId()
                        + "\nActiveClients : " + tarinfo.getActiveClients()
                        + "\nActivities : " + tarinfo.getActivities()
                        + "\nDefaultChannel : " + tarinfo.getDefaultChannel()
                        + "\nOnlineStatus : " + tarinfo.getOnlineStatus()
                        + "\nRoles : " + tarinfo.getRoles()
                        + "\nBoostContribution : " + tarinfo.getTimeBoosted()
                        + "\nJoined : " + tarinfo.getTimeJoined()
                        + "\nCreated : " + tarinfo.getTimeCreated()).queue();
            }
        }

        //- pick one from list of objects
        if(message.startsWith("!pk"))
        {
            int k = message.length();
            double randnum = Math.random();
            int vari = 1;
            String response ="";
            String submessage ="";

            if(k==3)//command help
            {
                response = "Usage : !pk object1,object2, ... ";
            }
            else//execute
            {
                for(int i = 4; i < k; i++)
                {
                    response = message.substring(i, i + 1);
                    if(response.equals(","))
                    {
                        vari = vari + 1;
                    }
                }
                submessage = message.substring(4, k);
                int pernum = (int)(randnum*vari)+1;
                response = submessage.split(",")[pernum-1];
            }
            event.getTextChannel().sendMessage(response).queue();
        }


        //- returns a number between 1~100
        if(message.startsWith("!random"))
        {
            double randnum = Math.random();
            int intnum = (int)(randnum*100)+1;
            event.getTextChannel().sendMessage(String.valueOf(intnum)).queue();
        }


        //- schedule registration
        if(message.startsWith("!sc"))
        {
            if(message.startsWith("!sc list") )
            {
                List<String> newsc = new ArrayList<>();
                List<String> sclcopy = timeshot.sclist;
                for(int  j=0;  j<sclcopy.size(); j++)
                {
                    newsc.add(sclcopy.get(j));
                }
                newsc.remove(0);
                if(newsc.size()==0)
                {
                    event.getTextChannel().sendMessage("The schedule list is currently empty").queue();
                }
                else
                {
                    List<String> newsca = timeshot.xremoval(newsc);
                    String alarm = newsca.toString().replace(",","\n");
                    alarm = alarm.substring(1,alarm.length()-1);

                    try{
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(Color.cyan);
                        eb.setTitle("Schedule List");
                        eb.setDescription(alarm);
                        MessageEmbed embed = eb.build();
                        event.getTextChannel().sendMessage(embed).queue();
                    }catch(NullPointerException e){
                        e.printStackTrace();
                    }
                }

            }
            else if(message.startsWith("!sc delete") )
            {
                List<String> newsc = new ArrayList<>();
                for(int  i=1;  i<timeshot.sclist.size(); i++)
                {
                    newsc.add(timeshot.sclist.get(i));
                }
                List<String> newsca = timeshot.xremoval(newsc);
                int delnum = Integer.parseInt(message.split(" ",3)[2]);

                if(delnum<=newsc.size() && delnum>0)//delete request reservation
                {
                    timeshot.scdelete.add(newsc.get(delnum-1));
                    timeshot.dlshot = 1;
                    event.getTextChannel().sendMessage("Delete request queued for " + newsca.get(delnum-1) ).queue();
                }
            }
            else if(message.length()>3 )
            {
                String msg = "null";
                try{
                    msg = message.split(" ",2)[1];
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                if(!msg.equals("null"))
                {
                    int res = timeshot.schsender(msg);
                    if(res==1)
                    {
                        event.getTextChannel().sendMessage("Wrong request").queue();
                    }
                    else
                    {
                        event.getTextChannel().sendMessage("Request queued").queue();
                    }
                }

            }
            else
            {
                event.getTextChannel().sendMessage("Input format : yyyy/MM/dd + HH:mm + content\nex) !sc 2021/08/04 14:45 Group flight").queue();
            }
        }

    }
}
