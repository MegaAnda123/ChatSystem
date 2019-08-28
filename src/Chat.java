public class Chat {

    private int chat_id;
    private Message[] messages;

    public Chat(int id){

        this.chat_id = id;


    }
    // adds Message object to back of array
    public void addMessage(Message message){
        int leng = messages.length;
        messages[leng] = message;
    }

    // returns message object at specific index
    public Message getMessageIn(int in){
        return messages[in];
    }
    // returns all message objects
    public Message[] getAllMessages(){
        return messages;
    }

}
