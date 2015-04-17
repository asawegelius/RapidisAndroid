package se.wegelius.routedisplayer;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnStatusChangedListener.STATUS;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.CompositeSymbol;
import com.esri.core.symbol.FontWeight;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleLineSymbol.STYLE;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.TextSymbol.HorizontalAlignment;
import com.esri.core.symbol.TextSymbol.VerticalAlignment;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

public class MapViewFragment
        extends Fragment
{
    private View calloutView;
    private Geometry geometry;
    private LocationDisplayManager locationDisplayManager;
    private SpatialReference mapSpatialReference;
    private MapView mapView;
    private GraphicsLayer myGraphicsLayer;

    public void displayDirections()
    {
        this.myGraphicsLayer.removeAll();
        GPFeatureRecordSetLayer route = Routes.getInstance().getRoute();
        GPFeatureRecordSetLayer routeElements = Routes.getInstance().getRouteElements();
        if (route != null)
        {
            this.mapView.pause();
            Iterator iterator = route.getGraphics().iterator();
            while (iterator.hasNext())
            {
                Graphic graphic = (Graphic)iterator.next();
                SimpleLineSymbol localSimpleLineSymbol = new SimpleLineSymbol(getResources().getColor(R.color.orange), 5.0F, SimpleLineSymbol.STYLE.SOLID);
                Polyline localPolyline = (Polyline)graphic.getGeometry();
                if (!route.getSpatialReference().equals(this.mapSpatialReference))
                {
                    new GeometryEngine();
                    localPolyline = (Polyline)GeometryEngine.project(graphic.getGeometry(), route.getSpatialReference(), this.mapSpatialReference);
                }
                Graphic localGraphic4 = new Graphic(localPolyline, localSimpleLineSymbol, graphic.getAttributes());
                this.myGraphicsLayer.addGraphic(localGraphic4);
                this.geometry = localPolyline;
            }
            ArrayList graphics = routeElements.getGraphics();
            System.out.println(graphics.size());
            for (int i = -1 + graphics.size(); i >= 0; i--)
            {
                PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(getActivity(), getResources().getDrawable(R.drawable.ic_map_pin));
                pictureMarkerSymbol.setOffsetY(20.0F);
                TextSymbol sequenceNumber = new TextSymbol(12, ((Graphic)graphics.get(i)).getAttributeValue("SequenceNumber").toString(), getResources().getColor(R.color.orange), TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.MIDDLE);
                sequenceNumber.setFontWeight(FontWeight.BOLD);
                sequenceNumber.setOffsetY(28.0F);
                ArrayList symbols = new ArrayList();
                symbols.add(pictureMarkerSymbol);
                symbols.add(sequenceNumber);
                CompositeSymbol compositeSymbol = new CompositeSymbol(symbols);
                Graphic graphic = new Graphic(((Graphic)graphics.get(i)).getGeometry(), compositeSymbol);
                if (!routeElements.getSpatialReference().equals(this.mapSpatialReference))
                {
                    new GeometryEngine();
                    graphic = new Graphic(GeometryEngine.project(((Graphic)graphics.get(i)).getGeometry(), routeElements.getSpatialReference(), this.mapSpatialReference), compositeSymbol);
                }
                Graphic newGraphic = new Graphic(graphic.getGeometry(), compositeSymbol, ((Graphic)graphics.get(i)).getAttributes());
                this.myGraphicsLayer.addGraphic(newGraphic);
            }
        }
    }

    public void launchGps(Graphic paramGraphic)
    {
        Location localLocation = this.locationDisplayManager.getLocation();
        new GeometryEngine();
        Point localPoint = (Point)GeometryEngine.project(paramGraphic.getGeometry(), this.mapView.getSpatialReference(), SpatialReference.create(4326));
        Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://maps.google.com/maps?saddr=" + localLocation.getLatitude() + "," + localLocation.getLongitude() + "&daddr=" + localPoint.getY() + "," + localPoint.getX() + "&mode=driving"));
        localIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(localIntent);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.map_view_layout, container, false);
        this.calloutView = View.inflate(getActivity(), 2130968601, null);
        this.mapView = ((MapView)view.findViewById(R.id.map));
        // checks if the map is instantiated. When it is sets the maps spatial reference and the route if it is uploaded
        this.mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            private static final long serialVersionUID = 1L;

            public void onStatusChanged(Object source, STATUS status) {
                if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == MapViewFragment.this.mapView) {
                    mapSpatialReference = MapViewFragment.this.mapView.getSpatialReference();
                    if (MapViewFragment.this.geometry != null) {
                        MapViewFragment.this.mapView.setExtent(MapViewFragment.this.geometry, 50);
                    }
                }
            }
        });
        this.mapView.setOnSingleTapListener(new OnSingleTapListener()
        {
            private static final long serialVersionUID = 1L;

            public void onSingleTap(float paramAnonymousFloat1, float paramAnonymousFloat2)
            {
                Callout callout = MapViewFragment.this.mapView.getCallout();
                callout.hide();
                int[] arrayOfInt = MapViewFragment.this.myGraphicsLayer.getGraphicIDs(paramAnonymousFloat1, paramAnonymousFloat2, 25);
                if ((arrayOfInt != null) && (arrayOfInt.length > 0))
                {
                    Graphic localGraphic = MapViewFragment.this.myGraphicsLayer.getGraphic(arrayOfInt[0]);
                    if (localGraphic.getGeometry().getType().equals(Geometry.Type.POINT))
                    {
                        Point localPoint = (Point)localGraphic.getGeometry();
                        callout.setOffset(0, 48);
                        callout.setCoordinates(localPoint);
                        MapViewFragment.this.updateContent(localGraphic.getAttributeValue("Description").toString(), localGraphic.getAttributeValue("StopType").toString(), localGraphic);
                        callout.setContent(MapViewFragment.this.calloutView);
                        callout.show();
                    }
                }
            }
        });
        this.myGraphicsLayer = new GraphicsLayer();
        this.mapView.addLayer(this.myGraphicsLayer);
        this.locationDisplayManager = this.mapView.getLocationDisplayManager();
        setMap();
        view.setTag("tab1");
        return view;
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

    public void onPause()
    {
        super.onPause();
        this.mapView.pause();
    }

    public void onResume()
    {
        super.onResume();
        if (Routes.getInstance().getRoute() == null) {
            this.mapView.unpause();
        }
    }

    public void setMap()
    {
        this.locationDisplayManager = this.mapView.getLocationDisplayManager();
        this.locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);
        this.locationDisplayManager.setLocationListener(new LocationListener()
        {
            boolean locationChanged = false;

            public void onLocationChanged(Location paramAnonymousLocation)
            {
                if (!this.locationChanged)
                {
                    Point localPoint = (Point)GeometryEngine.project(new Point(paramAnonymousLocation.getLongitude(), paramAnonymousLocation.getLatitude()), SpatialReference.create(4326), MapViewFragment.this.mapView.getSpatialReference());
                    float f = 100.0F;
                    if (paramAnonymousLocation.hasAccuracy()) {
                        f = paramAnonymousLocation.getAccuracy();
                    }
                    Unit localUnit = MapViewFragment.this.mapView.getSpatialReference().getUnit();
                    double d = 1.0D * Unit.convertUnits(f, Unit.create(9001), localUnit);
                    Envelope localEnvelope = new Envelope(localPoint, d, d);
                    MapViewFragment.this.mapView.setExtent(localEnvelope, 0, false);
                    this.locationChanged = true;
                    MapViewFragment.this.locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.NAVIGATION);
                    MapViewFragment.this.locationDisplayManager.setNavigationPointHeightFactor(0.35F);
                }
            }

            public void onProviderDisabled(String paramAnonymousString) {}

            public void onProviderEnabled(String paramAnonymousString) {}

            public void onStatusChanged(String paramAnonymousString, int paramAnonymousInt, Bundle paramAnonymousBundle) {}
        });
        this.locationDisplayManager.start();
        displayDirections();
    }

    public void updateContent(String title, String description, final Graphic paramGraphic)
    {
        if (this.calloutView == null) {
            return;
        }
        ((TextView)this.calloutView.findViewById(R.id.callout_title)).setText(title);
        ((TextView)this.calloutView.findViewById(R.id.callout_description)).setText(description);
        ImageButton localImageButton = (ImageButton)this.calloutView.findViewById(R.id.callout_image);
        localImageButton.setImageResource(R.drawable.ic_info);
        localImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                MapViewFragment.this.launchGps(paramGraphic);
            }
        });
    }
}