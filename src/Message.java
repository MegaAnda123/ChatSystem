public class Message {

    private String message;
    private String Date;
    private String Sender;
    private String reciver;


    public Message(String data, String Date, String Sender, String reciver){


        this.message = data;
        this.Date = Date;
        this.Sender = Sender;
        this.reciver = reciver;



    }

    public String getMessage(){
        return message;
    }

    public String getDate(){
        return Date;
    }

    public String getSender(){
        return Sender;
    }

    public String getReciver() {
        return reciver;
    }
}
