# PizzaDronz

A drone delivery system from the School of Informatics at the University of Edinburgh.

This program consumes the PizzaDronz API to retrieve orders for the given date, and calculates the shortest path between 
Appleton Tower <> each restaurant from which an order has been placed.

## Running the program

This program accepts the following arguments:

- [0]: the date for which to process orders (`yyyy-MM-dd`)
- [1]: the base API URL 

## Output

This program outputs 3 files under the `{projectRoot}/resultfiles/` directory:

- `deliveries-yyyy-MM-dd.json`: contains all the processed orders for the given date
- `flightpath-yyyy-MM-dd.json`: contains every move made by the drone for the given date
- `drone-yyyy-MM-dd.geojson`: contains the drone's flight path for the given date (GeoJSON format)

## Changelog

### v1.0.0 (2023-12-01)

- Initial release