/* gvSIG. Geographic Information System of the Valencian Government
*
* Copyright (C) 2007-2008 Infrastructures and Transports Department
* of the Valencian Government (CIT)
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
* MA  02110-1301, USA.
* 
*/

/*
* AUTHORS (In addition to CIT):
* 2011 Software Colaborativo (www.scolab.es)   development
*/
 
package org.gvsig.gpx.driver;

import java.awt.Shape;
import java.io.File;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import net.divbyzero.gpx.GPX;
import net.divbyzero.gpx.Route;
import net.divbyzero.gpx.Track;
import net.divbyzero.gpx.TrackSegment;
import net.divbyzero.gpx.Waypoint;
import net.divbyzero.gpx.parser.JDOM;
import net.divbyzero.gpx.parser.Parser;
import net.divbyzero.gpx.parser.ParsingException;

import com.hardcode.gdbms.driver.exceptions.CloseDriverException;
import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.driver.exceptions.WriteDriverException;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.MemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;

public class GPXVectorialDriver extends MemoryDriver implements VectorialFileDriver {
	public static final String DRIVERNAME = "GPX Driver";
	private static final int ID_FIELD_NAME = 0;
	private static final int ID_FIELD_CMT = 1;
	private static final int ID_FIELD_DESC = 2;
	private static final int ID_FIELD_ELEV = 3;
	private static final int ID_FIELD_TIME = 4;
	private static final int ID_FIELD_URL = 5;
	private static final int ID_FIELD_SYMBOL = 6;
	private static final int ID_FIELD_TYPE = 7;
	
	static FieldDescription[] fields = new FieldDescription[8];
	static {
		FieldDescription fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("name");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(254);
		fieldDesc.setFieldDecimalCount(0);
		fields[0] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("cmt");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(254);
		fieldDesc.setFieldDecimalCount(0);
		fields[1] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("desc");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(254);
		fieldDesc.setFieldDecimalCount(0);
		fields[2] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("elev");
		fieldDesc.setFieldType(Types.DOUBLE);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(5);
		fields[3] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("time");
		fieldDesc.setFieldType(Types.BIGINT);
		fieldDesc.setFieldLength(20);
		fieldDesc.setFieldDecimalCount(0);
		fields[4] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("url");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(254);
		fieldDesc.setFieldDecimalCount(0);
		fields[5] = fieldDesc;

		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("symbol");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(254);
		fieldDesc.setFieldDecimalCount(0);
		fields[6] = fieldDesc;
		
		fieldDesc = new FieldDescription();
		fieldDesc.setFieldName("type");
		fieldDesc.setFieldType(Types.VARCHAR);
		fieldDesc.setFieldLength(50);
		fieldDesc.setFieldDecimalCount(0);
		fields[7] = fieldDesc;
		

	}

	private File m_Fich;
	private DriverAttributes attr = new DriverAttributes();
	
	/**
	 * In order to not loose all data associated with the waypoints.
	 */
	private boolean bLoadAllAsWaypoints = false;

	public boolean isbLoadAllAsWaypoints() {
		return bLoadAllAsWaypoints;
	}

	public void setbLoadAllAsWaypoints(boolean bLoadAllAsWaypoints) {
		this.bLoadAllAsWaypoints = bLoadAllAsWaypoints;
	}

	@Override
	public String getName() {
		return DRIVERNAME;
	}

	@Override
	public int getShapeType() {
		return FShape.MULTI;
	}

	public DriverAttributes getDriverAttributes() {
		return attr;
	}

	public boolean isWritable() {
		return false;
	}

	public int[] getPrimaryKeys() throws ReadDriverException {
		return null;
	}

	public void write(DataWare dataWare) throws WriteDriverException,
			ReadDriverException {
		
	}

	public boolean accept(File f) {
		return f.getName().toUpperCase().endsWith("GPX");
	}

	public void close() throws CloseDriverException {
		
	}

	public File getFile() {
		return m_Fich;
	}

	public void initialize() throws ReadDriverException {
		float heightText = 10;
		attr.setLoadedInMemory(true);

		Parser parser = new JDOM();
		GPX gpx;
		try {
			gpx = parser.parse(m_Fich);
		} catch (ParsingException e) {
			e.printStackTrace();
			throw new ReadDriverException(getName(), e);
		}


		
		ArrayList<String> arrayFields = new ArrayList<String>();
		arrayFields.add("name");
		arrayFields.add("cmt");
		arrayFields.add("desc");
		arrayFields.add("elev");
		arrayFields.add("time");
		arrayFields.add("url");
		arrayFields.add("symbol");
		arrayFields.add("type");
		Value[] auxRow = new Value[arrayFields.size()];

		getTableModel().setColumnIdentifiers(arrayFields.toArray());


		// Ahora las rellenamos.
		FShape aux;
		for (int id = 0; id < gpx.getWaypoints().size(); id++) {

			Waypoint wp = gpx.getWaypoints().get(id);

			addWayPoint(auxRow, wp, wp.getName());

		}
		for (int id = 0; id < gpx.getRoutes().size(); id++) {
			Route route = gpx.getRoutes().get(id);
			if (bLoadAllAsWaypoints) {
				String name = route.getName();
				for (Waypoint wp : route.getWaypoints()) {
					addWayPoint(auxRow, wp, name);
				}
			}
			else {
				String name = "" + id;
				if (!route.getName().equalsIgnoreCase(""))
					name = route.getName();
				auxRow[ID_FIELD_NAME] = createValue(name);
				auxRow[ID_FIELD_CMT] = createValue(null);
				auxRow[ID_FIELD_DESC] = createValue(route.getDesc());
				auxRow[ID_FIELD_ELEV] = ValueFactory.createValue(0);
				auxRow[ID_FIELD_TIME] = ValueFactory.createNullValue();
				auxRow[ID_FIELD_URL] = createValue(route.getUrl());
				auxRow[ID_FIELD_SYMBOL] = createValue(null);
				auxRow[ID_FIELD_TYPE] = createValue("route");
				
				createRow(auxRow, route.getWaypoints());
			}
		}
		for (int id = 0; id < gpx.getTracks().size(); id++) {
			Track track = gpx.getTracks().get(id);
			if (bLoadAllAsWaypoints) {
				String name = track.getName();
				for (TrackSegment seg : track.getSegments()) {
					for (Waypoint wp : seg.getWaypoints()) {
						addWayPoint(auxRow, wp, name);
					}
				}
			}
			else {
				String name = track.getName();
				for (int idSegment = 0; idSegment < track.getSegments().size(); idSegment++) {
					TrackSegment segment = track.getSegments().get(idSegment);
					if (name == null)
						name = "" + id;
					auxRow[ID_FIELD_NAME] = createValue(name);
					auxRow[ID_FIELD_CMT] = createValue(null);
					auxRow[ID_FIELD_DESC] = createValue(null);
					auxRow[ID_FIELD_ELEV] = ValueFactory.createValue(0);
					auxRow[ID_FIELD_TIME] = ValueFactory.createNullValue();
					auxRow[ID_FIELD_URL] = createValue(null);
					auxRow[ID_FIELD_SYMBOL] = createValue(null);
					auxRow[ID_FIELD_TYPE] = createValue("track");
	
					createRow(auxRow, segment.getWaypoints());
				}
			}
		}
		
	}

	private void addWayPoint(Value[] auxRow, Waypoint wp, String name) {
//		if (wp.getName() != null)
//			auxRow[ID_FIELD_NAME] = createValue(name + " - " + wp.getName());
//		else
			auxRow[ID_FIELD_NAME] = createValue(name);
		auxRow[ID_FIELD_CMT] = createValue(wp.getCmt());
		auxRow[ID_FIELD_DESC] = createValue(wp.getDesc());
		auxRow[ID_FIELD_ELEV] = ValueFactory.createValue(wp.getElevation());
		if (wp.getTime() == null)
			auxRow[ID_FIELD_TIME] = ValueFactory.createNullValue();
		else {
			Timestamp t = new Timestamp(wp.getTime().getTime());
			// TODO: HACER QUE EL DBF SOPORTE TIMESTAMP. POR AHORA LO METEMOS COMO LONG
			auxRow[ID_FIELD_TIME] = ValueFactory.createValue(t.getTime());
		}
		auxRow[ID_FIELD_URL] = createValue(wp.getUrl());
		auxRow[ID_FIELD_SYMBOL] = createValue(wp.getSymbol());
		auxRow[ID_FIELD_TYPE] = createValue(wp.getType());
		
		IGeometry geom = ShapeFactory.createPoint2D(wp.getCoordinate().getLongitude(),
				wp.getCoordinate().getLatitude());
		addGeometry(geom, auxRow);
	}

	
	/**
	 * We create a lineString using the waypoints (2D => Maybe we should create a 3D geom)
	 * @param auxRow
	 * @param waypoints
	 */
	private void createRow(Value[] auxRow, ArrayList<Waypoint> waypoints) {
		GeneralPathX gp = new GeneralPathX();
		for (int j = 0; j < waypoints.size(); j++) {
			Waypoint wp = waypoints.get(j);
			double x = wp.getCoordinate().getLongitude();
			double y = wp.getCoordinate().getLatitude();
			if (j==0)
				gp.moveTo(x, y);
			else
				gp.lineTo(x, y);
		}
					
		IGeometry geom = ShapeFactory.createPolyline2D(gp);
		addGeometry(geom, auxRow);
	}
	
	private Value createValue(String obj) {
		if (obj == null)
			return ValueFactory.createNullValue();
		else
			return ValueFactory.createValue(obj);		
	}

	@Override
	public int getFieldType(int i) throws ReadDriverException {
		return fields[i].getFieldType();
	}
	
	@Override
	public int getFieldWidth(int i){
	    return fields[i].getFieldLength();
	}


	public void open(File f) throws OpenDriverException {
		m_Fich = f;	
	}

}

