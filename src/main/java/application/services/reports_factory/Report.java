package application.services.reports_factory;

import application.dto.RestaurantDTO;

public interface Report {

    void generateReport(RestaurantDTO restaurant, String path);
}
