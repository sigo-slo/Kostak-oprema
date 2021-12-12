package si.kostakdd.tabela;
public class LineItem {
    public String inv_st;
    public String opis;
//    public String picUrl;
    public String row_num;
    public String status;
    public String v_uporabi;

    public LineItem(int i, String inv_st, String opis, String status, String v_uporabi) {

        this.row_num = i+ ".";
        this.inv_st = inv_st;
        this.opis = opis;
        this.status = status;
        this.v_uporabi = v_uporabi;
//        this.picUrl = picUrl;
    }
}
