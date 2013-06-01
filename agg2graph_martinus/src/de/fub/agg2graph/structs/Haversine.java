package de.fub.agg2graph.structs;

/**
 * Haversine Function from: http://williams.best.vwh.net/avform.htm
 * @author Martinus
 *
 */
public class Haversine {
	public static final double R = 6371; //6371
	
	/**
	 * Get Distance between two points in meters
	 * @param from
	 * @param to
	 * @return
	 */
	public double distanceTwoPoints(ILocation from, ILocation to) {
		return distanceTwoPoints(from.getLat(), from.getLon(), to.getLat(), to.getLon());
	}
	
	/**
	 * Get Distance between two points in meters
	 * @param fromLat
	 * @param fromLon
	 * @param toLat
	 * @param toLon
	 * @return
	 */
	public double distanceTwoPoints(double fromLat, double fromLon, double toLat, double toLon) {
		double dLat = Math.toRadians(toLat - fromLat);
		double dLon = Math.toRadians(toLon - fromLon);
		fromLat = Math.toRadians(fromLat);
		toLat = Math.toRadians(toLat);
		
		double a = Math.pow( Math.sin(dLat/2), 2 ) + Math.pow( Math.sin(dLon/2), 2 ) * Math.cos(fromLat) * Math.cos(toLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
		return d * 1000;
	}
	
	public double initBearing(ILocation from, ILocation to) {
		return initBearing(from.getLat(), from.getLon(), to.getLat(), to.getLon());
	}
	
	public double initBearing(double fromLat, double fromLon, double toLat, double toLon) {
		double dLon = Math.toRadians(toLon - fromLon);
		
		fromLat = Math.toRadians(fromLat);
		toLat = Math.toRadians(toLat);
		fromLon = Math.toRadians(fromLon);
		toLon = Math.toRadians(toLon);
		
		double y = Math.sin(dLon) * Math.cos(toLat);
		double x = Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLon);
		double bearing = Math.atan2(y, x);
		bearing = (Math.toDegrees(bearing) + 360) % 360;
		return bearing;
	}
	
	public double finalBearing(ILocation from, ILocation to) {
		return finalBearing(to.getLat(), to.getLon(), from.getLat(), from.getLon());
	}
	
	public double finalBearing(double fromLat, double fromLon, double toLat, double toLon) {
		double dLon = Math.toRadians(toLon - fromLon);
		
		fromLat = Math.toRadians(fromLat);
		toLat = Math.toRadians(toLat);
		fromLon = Math.toRadians(fromLon);
		toLon = Math.toRadians(toLon);
		
		double y = Math.sin(dLon) * Math.cos(toLat);
		double x = Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLon);
		double bearing = Math.atan2(y, x);
		bearing = (Math.toDegrees(bearing) + 180) % 360;
		return bearing;
	}
	
	/**
	 * Get the middle Point
	 * @param from
	 * @param to
	 * @return
	 */
	public ILocation midPoint(ILocation from, ILocation to) {
		return midPoint(from.getLat(), from.getLon(), to.getLat(), to.getLon());
	}
	
	/**
	 * Get the middle Point
	 * @param fromLat
	 * @param fromLon
	 * @param toLat
	 * @param toLon
	 * @return
	 */
	public ILocation midPoint(double fromLat, double fromLon, double toLat, double toLon) {
		fromLat = Math.toRadians(fromLat);
		toLat = Math.toRadians(toLat);
		fromLon = Math.toRadians(fromLon);
		toLon = Math.toRadians(toLon);
		double bx = Math.cos(toLat) * Math.cos(toLon - fromLon);
		double by = Math.cos(toLat) * Math.sin(toLon - fromLon);
		double latM = Math.atan2(Math.sin(fromLat) + Math.sin(toLat), 
				Math.sqrt(Math.pow(Math.cos(fromLat) + bx, 2) + Math.pow(by, 2)));
		
		double lonM = fromLon + Math.atan2(by, Math.cos(fromLat) + bx);
		latM = Math.toDegrees(latM);
		lonM = Math.toDegrees(lonM);
		return new GPSPoint(latM, lonM);
	}
	
	public static void main(String[] args) {
		Haversine h = new Haversine();
		GPSPoint berlin = new GPSPoint(52.52564, 13.40185);
		GPSPoint potsdam = new GPSPoint(52.4, 13.066);

		GPSPoint p1 = new GPSPoint(52.51772574766543, 13.385655283927917);
		GPSPoint p2 = new GPSPoint(52.51784326353143, 13.385896682739258);
		System.out.println(h.distanceTwoPoints(p1, p2));
//		System.out.println("DISTANCE");
//		System.out.println(GPSCalc.getDistanceTwoPointsMeter(berlin, potsdam)/1000);
//		System.out.println(h.distanceTwoPoints(berlin, potsdam));
//		System.out.println("BEARING");
//		System.out.println(h.initBearing(berlin, potsdam));
//		System.out.println("MIDPOINT");
//		System.out.println(GPSCalc.getMidwayLocation(berlin, potsdam));
//		System.out.println(h.midPoint(berlin, potsdam));
//		System.out.println("TEST");
//		System.out.println(Math.toRadians(52.52564 - 52.4));
//		System.out.println(Math.toRadians(52.52564) - Math.toRadians(52.4));
		

	}
}
