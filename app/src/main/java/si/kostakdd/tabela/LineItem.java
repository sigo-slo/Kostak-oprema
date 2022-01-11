package si.kostakdd.tabela;

public class LineItem {
    public String row_num, inv_st, opis, geoLokacija, assignedTo;
    public int status;
    //public String Json;
    public boolean isExpanded;

    public LineItem(String row_num, String inv_st, String opis,  String geoLokacija, String assignedTo, int status) {

        this.row_num=row_num;
        this.inv_st=inv_st;
        this.opis=opis;
        //this.geoCoord=geoCoord;
        this.geoLokacija=geoLokacija;
        this.assignedTo=assignedTo;
        this.status=status;





    }
    public boolean getExpanded() {
        //Log.d("LINE_ITEM"+ row_num, "setExpandedReturned: "+isExpanded);
        return isExpanded;

    }
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
        //Log.d("LINE_ITEM"+row_num, "setExpanded: "+isExpanded);
    }

}
