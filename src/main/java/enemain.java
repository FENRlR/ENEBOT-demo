import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import javax.security.auth.login.LoginException;


public class enemain {

    public static JDA discord = null;

    static {
        try {
            discord = JDABuilder.createDefault(constants.discordToken)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS).setMaxReconnectDelay(32).setAutoReconnect(true).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args){

        discord.addEventListener(new response(),new googleres());

        /**
        /- run schedule manager as a sub process
        **/
        timeshot caller = new timeshot();
        caller.start();
    }
}
