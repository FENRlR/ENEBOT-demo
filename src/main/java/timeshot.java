import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import java.awt.Color;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class timeshot extends Thread {

    private static int caldswitch = 1;//test switch for local testing

    private static final String gld = constants.gld;//Guild id
    private static final String txtd = constants.txtd;//text channel id

    private static int testint = 1;//notifies any change from google docs
    private static int arshot = 0;//triggers alarm
    public static int dlshot = 0;//triggers actual delete process
    private static int brtrigger = 0;//tells weekly reminder briefed or not

    private static final String datesep = "\u000B∏∃";//separation indicator

    public static List<String> sclist = new ArrayList<>();//-schedule list
    public static Vector<String> scdelete = new Vector<>();//-delete reservation list
    
    private static int rcex = 0;//for time check


    //- sends the received input to google doc
    public static int schsender(String msg){
        try{
            int[] convone = splitter(msg);//int list conversion

            ZoneId timezone = ZoneId.of("Asia/Seoul");//Korean timezone
            LocalDateTime kortime = LocalDateTime.now(timezone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");//in yyyy/MM/dd HH:mm format
            String returnko = kortime.format(formatter); //current time for comparison
            int[] convtwo = splitter(returnko);

            //filtering part - check if the input follows the correct time format
            if(convone[1]>12 || convone[1]<1 || convone[2]<1 || convone[3]>23 || convone[4]>59)
            {
                return 1;//discard
            }
            else
            {
                String dateString = strsum(convone[0],convone[1],1);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/M/d");
                LocalDate tempd = LocalDate.parse(dateString, dateFormat);
                LocalDate maxdate = tempd.withDayOfMonth(tempd.getMonth().length(tempd.isLeapYear()));//the maximum day of the month
                int maxday = Integer.parseInt(maxdate.toString().split("-",3)[2]);
                if(convone[2]>maxday)//if exceeds
                {
                    return 1;
                }
            }
            int onesum = intsum(convone[0],convone[1],convone[2]);//input
            int twosum = intsum(convtwo[0],convtwo[1],convtwo[2]);//current time

            if(onesum<twosum)//if the input is a past date
            {
                return 1;
            }
            else if(onesum==twosum)//if the input is a past time of same date
            {
                onesum = convone[3]*60 + convone[4];
                twosum = convtwo[3]*60 + convtwo[4];
                if(onesum<=twosum)
                {
                    return 1;
                }
            }
            //send
            String nmsg = datesep + "X," + msg;
            googleres.doctel(0, nmsg);

            testint = 1;//update signal

            return 0;

        }catch(Exception e){
            e.printStackTrace();
            return 1;
        }
    }


    //- Schedule check thread
    public void run(){
        int rawdint = 0;
        while(caldswitch == 1)
        {
            if(testint==1)//checks update signal and reloads sclist from google docs
            {
                String dndate = googleres.doctel(1, "");//doctel(1) returns the entire document as a string
                sclist = new ArrayList<String>(Arrays.asList(dndate.split(datesep)));//remove date separators and make the data as a list. the 0th element is the Id part
                rawdint = dndate.length();//total length
                String[] tempod = sclist.get(0).split(",",2);
                testint = 0;//updated
            }

            //seeks delete request
            int subdlshot = 0;
            List<String> subdelete = new ArrayList<>();
            
            if(dlshot ==1)
            {
                dlshot = 0;
                subdlshot = 1;
                int stai = scdelete.size();
                for(int i=0; i<stai; i++)
                {
                    subdelete.add(scdelete.get(i));//gets the content of the selected element from delete request
                }
                for(int i=stai-1; i>=0; i--)
                {
                    scdelete.remove(i);
                }
            }

            //date check
            ZoneId timezone = ZoneId.of("Asia/Seoul");//Korean timezone
            LocalDateTime kortime = LocalDateTime.now(timezone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            int[] returnko = splitter(kortime.format(formatter));//current time for comparison

            if(sclist.size()>1)
            {
                List<Integer> start = new ArrayList<>();//start index list of deletion
                List<Integer> end = new ArrayList<>();//end index list of deletion
                List<Integer> backtodo = new ArrayList<>();//for internal sync

                for(int i=1; i< sclist.size(); i++)//0th element is the guild id part
                {
                    String[] scstr = sclist.get(i).split(",",2);//"X" for future usage

                    if(scstr[0].equals("X"))//"X" for future usage
                    {
                        int[] combms = splitter(scstr[1]);//0:year, 1:month, 2:day, 3:hour, 4:minute
                        rcex = 0;
                        scvs(0,returnko,combms);

                        String rst="";//current time
                        String cst="";//comparison
                        for(int j = 0; j<5; j++)
                        {
                            rst = rst + zradd(Integer.toString(returnko[j]));
                            cst = cst + zradd(Integer.toString(combms[j]));
                        }

                        if(subdlshot==1)//calculate start & end index
                        {
                            for(int j=0; j<subdelete.size(); j++)
                            {
                                if(subdelete.get(j).equals(sclist.get(i)))
                                {
                                    start.add(cumst(i)+1);
                                    int temt = cumst(i)+3+sclist.get(i).length()+1;
                                    if(temt>rawdint)
                                    {
                                        end.add(temt-1);
                                    }
                                    else
                                    {
                                        end.add(temt);
                                    }
                                    backtodo.add(i);
                                }
                            }
                            if(i==sclist.size()-1)//if i is the end index
                            {
                                subdelete.clear();
                                subdlshot = 0;
                            }

                        }
                        else if(rcex==5)//time checking - if it is the exact date and time
                        {
                            String alarm = scstr[1].split(" ",3)[2];//title
                            String alcont = "Scheduled for " + scstr[1].split(" ",3)[0].split("/",2)[1] + " " + scstr[1].split(" ",3)[1];//content

                            JDA discord = enemain.discord;
                            try{
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setColor(Color.cyan);
                                eb.setTitle(alarm);
                                eb.setDescription(alcont);
                                MessageEmbed embed = eb.build();
                                discord.getGuildById(gld).getTextChannelById(txtd).sendMessage(embed).queue();//send
                                discord.getGuildById(gld).getTextChannelById(txtd).sendMessage("@everyone").queue();//mentions everyone
                            }catch(NullPointerException e){
                                e.printStackTrace();
                            }

                            //- delete sent schedules
                            //minimal start index : 1, maximum end index : raw total length + 1
                            start.add(cumst(i)+1);
                            int temt = cumst(i)+3+sclist.get(i).length()+1;
                            if(temt>rawdint)//but maximum end index of delete request should not exceed "raw total length + 1"
                            {
                                end.add(temt-1);
                            }
                            else
                            {
                                end.add(temt);
                            }
                            backtodo.add(i);//for internal sync
                        }
                        else if(Long.parseLong(rst)>Long.parseLong(cst))//delete request for past date - may exist due to connection loss
                        {
                            start.add(cumst(i)+1);
                            int temt = cumst(i)+3+sclist.get(i).length()+1;
                            if(temt>rawdint)
                            {
                                end.add(temt-1);
                            }
                            else
                            {
                                end.add(temt);
                            }
                            backtodo.add(i);
                        }
                    }
                }

                if(backtodo.size()>0)//- passes delete queue to delete process
                {
                    //minimal start index : 1, maximum end index : raw total + 1
                    googleres.docdel(start,end);
                    testint=1;//update signal

                    //- internal list synchronization
                    for(int i = backtodo.size()-1; i>=0; i--)
                    {
                        int ri = backtodo.get(i);
                        sclist.remove(ri);
                    }
                }
            }
            

            //- Weekly reminder
            String cday = kortime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);//current name of day

            if( cday.equals("Monday") && sclist.size()>1 && returnko[3]==12 && returnko[4]==0 && brtrigger==0
                    || cday.equals("Thursday") && sclist.size()>1 && returnko[3]==12 && returnko[4]==0 && brtrigger==0)//sends a reminder on every Monday and Thursday, 12:00
            {
                List<String> newsc = new ArrayList<>();//sclist cloning except its 0th element(which contains IDs)
                for(int  j=1;  j<sclist.size(); j++)
                {
                    newsc.add(sclist.get(j));
                }
                List<String> newsca = rqspinit(xremoval(newsc));
                String alarm = newsca.toString().replace(",","\n");
                alarm = alarm.substring(1,alarm.length()-1);

                //- send message
                JDA discord = enemain.discord;
                try{
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(Color.cyan);
                    eb.setTitle("Upcoming Schedule");
                    eb.setDescription(alarm);
                    MessageEmbed embed = eb.build();
                    discord.getGuildById(gld).getTextChannelById(txtd).sendMessage(embed).queue();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                brtrigger=1;//To avoid multiple reminders
            }
            else if(!cday.equals("Monday") && brtrigger==1 ||!cday.equals("Thursday") && brtrigger==1)
            {
                brtrigger=0;
            }

            //- rest till the next minute
            try{
                kortime = LocalDateTime.now(timezone);
                returnko = splitter(kortime.format(formatter));
                TimeUnit.SECONDS.sleep(60-returnko[4]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    //--------------------------
    //------ Tool Methods ------
    //--------------------------
    //- Cumulative starting point index
    public static int cumst(int sti){
        int cint = 0;
        for(int i=0; i<sti; i++)
        {
            cint += sclist.get(i).length();
        }
        return cint + 3*(sti-1);
    }

    //- zero adder for int conversion
    public static String zradd(String num){
        if(!num.startsWith("0"))
        {
            if(Integer.parseInt(num)<10)
            {
                num = "0"+num;
            }
        }
        else if(num.equals("0"))
        {
            num = "00";
        }
        return num;
    }

    //- zero removal for int conversion
    public static String zrem(String num){
        if(num.startsWith("0")&&!num.equals("00"))
        {
            num = num.substring(1);
        }
        else if(num.equals("00")) //for 00 hour and 00 min
        {
            num = "0";
        }
        return num;
    }

    //- gets year, month, and day. convert them into yyyy/mm/dd format if not, and merge them into a number
    public static int intsum(int a, int b, int c){
        String total = Integer.toString(a) + zradd(Integer.toString(b)) + zradd(Integer.toString(c));
        return Integer.parseInt(total);
    }

    //- gets integers and return them as a string with slashes
    public static String strsum(int a, int b, int c){
        return Integer.toString(a) + "/" + Integer.toString(b) + "/" +Integer.toString(c);
    }

    //- splitter : message to int array
    public static int[] splitter(String date){//example of date : "2020/07/28 6:23 abcde"
        int[] ymdhm = new int[5]; //To break it into pieces - y , m , d , h , m
        String ydate = date.split(" ",3)[0];
        String ytime = date.split(" ",3)[1];
        for(int i=0; i<5; i++)//0~4. 0,1,2 = y,m,d. 3,4 = h,m
        {
            if(i<3)
            {
                ymdhm[i] = Integer.parseInt(zrem(ydate.split("/",3)[i]));//y , m , d
            }
            else
            {
                ymdhm[i] = Integer.parseInt(zrem(ytime.split(":",2)[i-3]));//h , m
            }
        }
        return ymdhm;
    }

    //- date comparison from string
    private static long sumsplt(String a){
        int[] b = splitter(a);
        String c = "";
        for(int i =0; i<5; i++)
        {
            c = c + zradd(Integer.toString(b[i]));
        }
        return Long.parseLong(c);

    }

    //- time checker
    private static void scvs(int x,int[] a,int[] b){
        if(x<5)
        {
            if(a[x]==b[x])
            {
                scvs(x+1, a, b);
            }
            else
            {
                rcex = x;
            }
        }
        else
        {
            rcex = x;//if all the numbers are the same
        }
    }


    //- Sorting function for weekly reminder (merge sort)
    private List<String> orgmerge(List<String> a, List<String> b){
        List<String> c = new ArrayList<>();
        int i =0;
        int j =0;
        while(i<a.size()||j<b.size())
        {
            if(i>=a.size()||j>=b.size())
            {
                if(i>=a.size())
                {
                    c.add(b.get(j));
                }
                else
                {
                    c.add(a.get(i));
                }
                break;
            }
            else
            {
                if(sumsplt(a.get(i))<=sumsplt(b.get(j)))
                {
                    c.add(a.get(i));
                    i++;
                }
                else
                {
                    c.add(b.get(j));
                    j++;
                }
            }
        }
        return c;
    }


    //- part 2 (merge sort)
    private List<String> rqspinit(List<String> a){
        List<String> b;
        List<String> c;
        if(a.size() != 1)
        {
            //divide
            b = rqspinit(new ArrayList<String>(a.subList(0,(a.size()-1)/2 + 1)));//0~half
            c = rqspinit(new ArrayList<String>(a.subList((a.size()-1)/2 + 1,a.size() )));
            return orgmerge(b,c);
        }
        else
        {
            return a;
        }
    }


    //- x removal
    public static List<String> xremoval(List<String> a){
        List<String> b = new ArrayList<>();//store return
        int limit = a.size()-1;
        for(int i=0; i<limit+1; i++)
        {
            b.add(a.get(i).split(",",2)[1]);
        }
        return b;
    }


}
