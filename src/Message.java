public class Message {

    private String data;
    private String Date;
    private String Sender;


    public Message(String data, String Date, String Sender){


        this.data = data;
        this.Date = Date;
        this.Sender = Sender;


    }

    public String getData(){
        return data;
    }

    public String getDate(){
        return Date;
    }

    public String getSender(){
        return Sender;
    }
}
