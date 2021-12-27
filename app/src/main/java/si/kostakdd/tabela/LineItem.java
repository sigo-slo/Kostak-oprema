package si.kostakdd.tabela;
public class LineItem {
    public String inv_st;
    public String opis;
    public String row_num;


    public LineItem(int i, String inv_st, String opis) {

        this.row_num = i+ ".";
        this.inv_st = inv_st;
        this.opis = opis;

    }
}
