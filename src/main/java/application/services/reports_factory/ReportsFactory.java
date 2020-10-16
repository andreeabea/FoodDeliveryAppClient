package application.services.reports_factory;

public class ReportsFactory {

    public Report getReport(String reportType)
    {
        if(reportType==null)
        {
            return null;
        }
        if(reportType.equals("pdf"))
        {
            return new PdfReport();
        }
        else if(reportType.equals("txt"))
        {
            return new TxtReport();
        }

        return null;
    }
}
