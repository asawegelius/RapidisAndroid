package se.wegelius.routedisplayer;

import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import java.util.ArrayList;
/*
    A singelton class that contains the route its routeElements and reccord of if they are visited or not
 */
public class Routes
{
    private static Routes instance;
    private GPFeatureRecordSetLayer route;
    private GPFeatureRecordSetLayer routeElements;
    private boolean[] visited;

    public static Routes getInstance()
    {
        if (instance == null) {
            instance = new Routes();
        }
        return instance;
    }

    public GPFeatureRecordSetLayer getRoute()
    {

        return instance.route;
    }

    public GPFeatureRecordSetLayer getRouteElements()
    {

        return instance.routeElements;
    }

    public boolean getVisited(int position)
    {

        return this.visited[position];
    }

    public void setRoute(GPFeatureRecordSetLayer paramGPFeatureRecordSetLayer)
    {
        this.route = paramGPFeatureRecordSetLayer;
    }

    public void setRouteElements(GPFeatureRecordSetLayer paramGPFeatureRecordSetLayer)
    {
        this.routeElements = paramGPFeatureRecordSetLayer;
        this.visited = new boolean[paramGPFeatureRecordSetLayer.getGraphics().size()];
    }

    public void setVisited(int position)
    {

        this.visited[position] = true;
    }
}
