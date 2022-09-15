import com.google.api.services.docs.v1.model.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;


public class googleres extends ListenerAdapter {

    private static final String APPLICATION_NAME = "enebot";//application name
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();//JacksonFactory.getDefaultInstance();

    private static final String CLIENT_SECRET_DIR = constants.CLIENT_SECRET_DIR;//client_secret from resources folder
    private static final String CREDENTIALS_SHEETS = constants.CREDENTIALS_SHEETS;//credential directory - for sheets
    private static final String CREDENTIALS_DOCS = constants.CREDENTIALS_DOCS;//credential directory - for docs

    //- scopes
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final List<String> DRSC = Collections.singletonList(DocsScopes.DOCUMENTS);

    private static final String SPREADSHEET_ID = constants.SPREADSHEET_ID;//initial sheet template : https://docs.google.com/spreadsheets/d/1OQh5dLw-XWO7iDTmyVEnajvOPEPpVTEvcDcTQLfl2tY
    private static final String scdocid = constants.scdocid;//initial document template : https://docs.google.com/document/d/1jO8EBavUM9D5IuTmonYx28xPlSNsPGZLVEx6wiIwO74

    //- Loads credentials & handles authorization
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final List<String> SCOPES, final String CREDENTIALS_FOLDER) throws IOException {

        InputStream in = googleres.class.getResourceAsStream(CLIENT_SECRET_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("enebot");

        return credential; //authorized credential object
    }

    //- user dictionary
    public void onMessageReceived(MessageReceivedEvent event) {

        String message = event.getMessage().getContentRaw();

        if (message.startsWith("!um")||message.startsWith("!ud"))//!um for word registration, !ud for word search
        {
            try {
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SCOPES, CREDENTIALS_SHEETS))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                int k = message.length();
                int index = 0;
                String word = "";
                String mean = "";
                String temp = "";
                String space = "";
                String name = "";

                String submessage = message.substring(1, 3);

                if(submessage.equals("um"))
                {
                    if (k > 3)
                    {
                        word = message.substring(4, k);
                        space = message.substring(3, 4);

                        if (space.equals(" "))
                        {
                            for (int i = 4; i < k; i++)
                            {
                                temp = message.substring(i, i + 1);

                                if((temp.equals(":") || temp.equals(" "))&&word.length()>0)
                                {
                                    index = i + 1;
                                    word = message.substring(4, index - 1);
                                    mean = message.substring(index, k);
                                    name = event.getAuthor().getName();
                                    Object a = new Object();//TODO: clean this mess
                                    a = word;
                                    Object b = new Object();
                                    b = mean;
                                    Object c = new Object();
                                    c = name;
                                    Object d = new Object();
                                    d = "x";//"x" for future usage
                                    String nnum = "";//position of new-word-placement
                                    String gnum = "";//position of existing word

                                    //Pre-search for existence check
                                    ValueRange scout = new ValueRange();
                                    scout.setValues(Arrays.asList(Arrays.asList(a)));
                                    UpdateValuesResponse result = service.spreadsheets().values()
                                            .update(SPREADSHEET_ID, "F1", scout)
                                            .setValueInputOption("RAW")
                                            .execute();

                                    //Get the position of the word from sheet
                                    ValueRange ready = service.spreadsheets().values().get(SPREADSHEET_ID, "G1").execute();
                                    gnum = ready.getValues().toString();
                                    gnum = gnum.substring(2, gnum.length() - 2);
                                    //output sample : {"majorDimension":"ROWS","range":"'sheet1'!G1","values":[["#N/A"]]}

                                    if (gnum.equals("#N/A"))//if such word does not exist, place the new word at the end
                                    {
                                        //Final posting - new word
                                        ready = service.spreadsheets().values().get(SPREADSHEET_ID, "N1").execute();
                                        nnum = ready.getValues().toString();
                                        nnum = nnum.substring(2, nnum.length() - 2);

                                        ValueRange body = new ValueRange();
                                        body.setValues(Arrays.asList(Arrays.asList(a, b, c, d)));
                                        result = service.spreadsheets().values()
                                                .update(SPREADSHEET_ID, "A" + nnum + ":D" + nnum, body)
                                                .setValueInputOption("RAW")
                                                .execute();
                                        event.getTextChannel().sendMessage("Registered a new word.").queue();
                                    }
                                    else//renewal of the existing word
                                    {
                                        ValueRange body = new ValueRange();
                                        body.setValues(Arrays.asList(Arrays.asList(a, b, c, d)));
                                        result = service.spreadsheets().values()
                                                .update(SPREADSHEET_ID, "A" + gnum + ":D" + gnum, body)
                                                .setValueInputOption("RAW")
                                                .execute();
                                        event.getTextChannel().sendMessage("Renewed an existing word.").queue();
                                    }
                                    i = k+100;

                                }
                                else if (i == k-1)
                                {
                                    event.getTextChannel().sendMessage("Please enter a proper command.").queue();
                                }
                            }

                        }
                        else
                        {
                            event.getTextChannel().sendMessage("Please enter a proper command.").queue();
                        }
                    }
                    else
                    {
                        event.getTextChannel().sendMessage("Usage : !um + word + meaning \nor... !um + word + : + meaning \nex) !um dog:is not a cat\nTo search a word, use **!um**").queue();
                    }

                }
                else if(submessage.equals("ud"))
                {
                    if (k > 3)
                    {
                        space = message.substring(3, 4);

                        if (space.equals(" "))
                        {
                            for (int i = 4; i < k; i++)
                            {
                                temp = message.substring(i, i + 1);

                                if (temp.equals(" "))
                                {
                                    event.getTextChannel().sendMessage("Please remove the spaces in the word part.").queue();
                                    i = k+100;
                                }
                                else if (i == k-1)
                                {
                                    word = message.substring(4, k);
                                    Object a = new Object();
                                    a = word;
                                    String gnum = "";

                                    //Pre-search for existence check
                                    ValueRange scout = new ValueRange();
                                    scout.setValues(Arrays.asList(Arrays.asList(a)));
                                    UpdateValuesResponse result = service.spreadsheets().values()
                                            .update(SPREADSHEET_ID, "F1", scout)
                                            .setValueInputOption("RAW")
                                            .execute();

                                    //Get the position of the word
                                    ValueRange ready = service.spreadsheets().values().get(SPREADSHEET_ID, "G1").execute();
                                    gnum = ready.getValues().toString();
                                    gnum = gnum.substring(2, gnum.length() - 2);

                                    if (gnum.equals("#N/A"))
                                    {
                                        event.getTextChannel().sendMessage("The requested word does not exist. To register a new word, use **!um**").queue();
                                    }
                                    else
                                    {
                                        //Get the word and its meaning based on the retrieved position
                                        ValueRange body = service.spreadsheets().values().get(SPREADSHEET_ID, "B" + gnum + ":C" + gnum).execute();

                                        int refindex = body.getValues().toString().split(",").length;

                                        mean = body.getValues().toString().split(",")[0];
                                        mean = mean.substring(2, mean.length());
                                        name = body.getValues().toString().split(",")[refindex-1];
                                        name = name.substring(1,name.length()-2);

                                        //Filtering out extra comma
                                        int pkl = 0;
                                        int bkl = body.getValues().toString().length();

                                        for(int hkl=0; hkl<body.getValues().toString().length(); hkl++)
                                        {
                                            if(body.getValues().toString().substring(hkl,hkl+1).equals(","))
                                            {
                                                pkl = pkl + 1;
                                            }
                                        }
                                        if(name.equals(" ")||pkl>1)//If the username does not exist due to some potential bug in future
                                        {
                                            name = body.getValues().toString().split(",")[pkl];
                                            mean = body.getValues().toString().substring(2, (bkl-name.length()-1));
                                            name = name.substring(1,name.length()-2);

                                            if(name.equals(" "))
                                            {
                                                name = "UNKNOWN%USER";
                                            }
                                        }

                                        if(mean.length()>5 && mean.substring(0,4).equals("http"))//if the meaning of the word starts with http
                                        {
                                            event.getTextChannel().sendMessage(mean + " by **" + name ).queue();
                                        }
                                        else
                                        {
                                            event.getTextChannel().sendMessage("**" + mean + "** by **" + name ).queue();
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            event.getTextChannel().sendMessage("Please enter a proper command.").queue();
                        }
                    }
                    else
                    {
                        event.getTextChannel().sendMessage("Usage : !ud + word \nex)!ud dog\nTo register a word, use **!um**").queue();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                event.getTextChannel().sendMessage("IOException occurred").queue();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                event.getTextChannel().sendMessage("GeneralSecurityException occurred").queue();
            }

        }
    }


    // - Manages connection with google docs
    public static String doctel(int condition, String conts){

        try{
            //- String scdocid refers to the document id
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRSC, CREDENTIALS_DOCS))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            if(condition == 0)//upload
            {
                List<Request> requests = new ArrayList<>();
                requests.add(new Request().setInsertText(new InsertTextRequest()
                        .setText(conts)
                        .setEndOfSegmentLocation(new EndOfSegmentLocation() )));

                BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);
                BatchUpdateDocumentResponse response = service.documents()
                        .batchUpdate(scdocid, body).execute();

                return "up-done";
            }
            else if(condition == 1)//download
            {
                // Prints the title of the requested doc:
                Document response = service.documents().get(scdocid).execute();
                String content = readStructuralElements(response.getBody().getContent());

                return content;
            }

        }catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        return "null";
    }

    //- Handles delete request
    public static String docdel(List<Integer> start, List<Integer> end){

        try{
            //- String scdocid refers to the document id.
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRSC, CREDENTIALS_DOCS))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            List<Request> requests = new ArrayList<>();

            for(int i = start.size()-1; i>=0; i--)//the delete request should be sent in reverse order
            {
                requests.add(new Request().setDeleteContentRange(
                        new DeleteContentRangeRequest()
                                .setRange(new Range()
                                        .setStartIndex(start.get(i))
                                        .setEndIndex(end.get(i)))
                ));

            }
            BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);
            BatchUpdateDocumentResponse response = service.documents()
                    .batchUpdate(scdocid, body).execute();

            return "deleted";

        }catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        return "null";
    }



    /**
     * - text extraction (retrieved from https://developers.google.com/docs/api/samples/extract-text)
     */
    //- returns the text in the given ParagraphElement
    private static String readParagraphElement(ParagraphElement element) {
        TextRun run = element.getTextRun();
        if (run == null || run.getContent() == null) {
            // The TextRun can be null if there is an inline object.
            return "";
        }
        return run.getContent();
    }

    //- recurses through a list of Structural Elements to read a document's text where text may be in nested elements
    private static String readStructuralElements(List<StructuralElement> elements) { //element : a ParagraphElement from a Google Doc, elements : a list of Structural Elements
        StringBuilder sb = new StringBuilder();
        for (StructuralElement element : elements) {
            if (element.getParagraph() != null) {
                for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
                    sb.append(readParagraphElement(paragraphElement));
                }
            } else if (element.getTable() != null) {
                // The text in table cells are in nested Structural Elements and tables may be nested.
                for (TableRow row : element.getTable().getTableRows()) {
                    for (TableCell cell : row.getTableCells()) {
                        sb.append(readStructuralElements(cell.getContent()));
                    }
                }
            } else if (element.getTableOfContents() != null) {
                // The text in the TOC is also in a Structural Element.
                sb.append(readStructuralElements(element.getTableOfContents().getContent()));
            }
        }
        return sb.toString();
    }
}
