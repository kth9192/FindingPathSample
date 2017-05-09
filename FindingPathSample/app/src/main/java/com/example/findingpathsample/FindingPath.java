package com.example.findingpathsample;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by kth91 on 2016-11-17.
 */

public class FindingPath {

    private String TAG = "FindingPath";
    private LatLng start; //시작점
    private LatLng end ; // 끝점
    private GoogleMap map;

    double distance;
    double distforHeuristic;
    private double mindist = 0;
    HashMap<String, LandMark> landMarkHashMap; // 노드 그래프정보

    LinkedHashMap<String, LandMark> pathMap = new LinkedHashMap<>(); // 최단경로 리스트
    HashMap<String, Integer> openMap = new HashMap<>(); // 열린 리스트
    //    HashMap<String, LandMark> closeMap = new HashMap<>(); // 닫힌 리스트
    HashMap<String, Double> resultMap = new HashMap<>(); // 결과 맵


    private HashMap<String, Double> distMap = new HashMap<>();
    private HashMap<String, Double> Heuristic = new HashMap<>();
    Polyline polyline;

    public FindingPath(LatLng start, LatLng end , HashMap<String, LandMark> landMarkMap, GoogleMap map){
        this.start = start;
        this.end = end;
        landMarkHashMap = landMarkMap;
        this.map = map;
    }

    //마커를 노드로 대응해 그래프 형성
    // g(x) + h(x). g(x)는 노드에서 연결된 각 노드당 거리 수치. h(x)는 각 노드에서 목적지까지 일직선 최단거리 값.
    // 시작노드에서 인접한 노드를 구분한 기준선정. - 인접구분없이 전체를 기준?
    // 시작노드에서 인접노드까지 거리를 구해 리스트화
    // 최단거리인 노드까지 이동 후 다시 인접노드 선정 및 거리계산 (라인을 그릴것이기 때문에 여기서의 이동은 라인용 노드 리스트에 추가하는 행동)
    // 목적지까지 반복.

    public void finding(){

        Location endNode = new Location("도착지노드");
        endNode.setLatitude(end.latitude);
        endNode.setLongitude(end.longitude);
        Location stdNode = new Location("최단거리 비교대상노드");
        Location startNode = new Location("기준노드");
        Location compareNode = new Location("비교대상노드");


        float standard = 15;

        //오픈리스트 초기화.
        for (String key : landMarkHashMap.keySet()){
            openMap.put(key, 0);
        }

        //h(x) 구하기.
        for(String keyforHeuristic  : landMarkHashMap.keySet()){

            stdNode.setLatitude(landMarkHashMap.get(keyforHeuristic).getLatLng().latitude);
            stdNode.setLongitude(landMarkHashMap.get(keyforHeuristic).getLatLng().longitude);

            distforHeuristic = endNode.distanceTo(stdNode)/1000;

            if((distforHeuristic) != 0)
                Heuristic.put(keyforHeuristic , distforHeuristic);

            Log.d(TAG, "키 " + keyforHeuristic + " 휴리스틱함수 값 "+ String.valueOf(Heuristic.get(keyforHeuristic)));
        }

        for (String key : landMarkHashMap.keySet()){

            if (start.equals(landMarkHashMap.get(key).getLatLng())) {
                //시작노드 넣어놓기
                pathMap.put(key, landMarkHashMap.get(key));
            }
        }

        loop:
        while (true) {

            if (start.equals(end)) { // 경로탐색 종료조건.
                //패스 검사 종료
                paintingPath();
                break loop;
            } else {
                Log.d(TAG, "시작" + start.latitude + " , " + start.longitude);
                Log.d(TAG, "끝" + end.latitude + " , " + end.longitude);

                //g(x)구하기.
                startNode.setLatitude(start.latitude);
                startNode.setLongitude(start.longitude);

//                CircleOptions circleOptions = new CircleOptions();
//                circleOptions.center(start).radius(rad)
//                        .visible(true);
//                map.addCircle(circleOptions);

                for (String key : landMarkHashMap.keySet()) {
                    if (!landMarkHashMap.get(key).getLatLng().equals(start) && (openMap.get(key) == 0)) {
                        compareNode.setLatitude(landMarkHashMap.get(key).getLatLng().latitude);
                        compareNode.setLongitude(landMarkHashMap.get(key).getLatLng().longitude);

                        distance = (startNode.distanceTo(compareNode)) / 1000;
                        Log.d(TAG, "비교용키셋 " + key);
                        Log.d("기준점까지 거리", String.valueOf(distance));
                        Log.d("반지름", String.valueOf(standard));

                    }

                    if (standard >= distance) { // 인접노드 인식 범위내 노드가 존재할 때
                        if (landMarkHashMap.get(key).getLatLng().equals(end)){
                            Log.d(TAG, "종료");
                            pathMap.put(key, landMarkHashMap.get(key));
                            paintingPath();
                            break loop;
                        }
                        Log.d(TAG, "통과키" + key + "기준통과" + String.valueOf(distance));
                        distMap.put(key, distance);
                    }
                    else ;

                }

                if (ObjectUtils.isEmpty(distMap)) {

                    Log.d(TAG, "노드실종");
                    for (String keyforend : landMarkHashMap.keySet()) {

                        if (end.equals(landMarkHashMap.get(keyforend).getLatLng())) {
                            //끝노드 넣어놓기
                            pathMap.put(keyforend, landMarkHashMap.get(keyforend));
                        }
                    }
                    paintingPath();
                    break loop;

                }

                for (String key : distMap.keySet()) { // g(x) + h(x) 값이 가장 작은 노드를 찾아서 결과 맵에 집어넣음.
                    //h(x) 는 각 노드에서 목표노드까지 추정 잔여거리 . g(x) 는 기준 노드 에서 각 노드까지의 거리.

                    resultMap.put(key, distMap.get(key) + Heuristic.get(key));

                    Log.d("키값", key);
                    Log.d("결과", String.valueOf(resultMap.get(key)));

                    if (mindist == 0)
                        mindist = resultMap.get(key);
                    else if (mindist > resultMap.get(key))
                        mindist = resultMap.get(key);
                    else
                        ;

                    Log.d(TAG, "최단거리" + mindist);
                    Log.d(TAG, "검사" + (mindist == resultMap.get(key)));

                }

                for (String key : resultMap.keySet()) {
                    if (resultMap.get(key) == mindist) {

                        if(landMarkHashMap.get(key).getLatLng().equals(start) ){
                            for (String keyforend : landMarkHashMap.keySet()){

                                if (end.equals(landMarkHashMap.get(keyforend).getLatLng())) {
                                    //끝노드 넣어놓기
                                    pathMap.put(keyforend, landMarkHashMap.get(keyforend));
                                }
                            }
                            paintingPath();
                            break loop;
                        }

                        pathMap.put(key, landMarkHashMap.get(key));

                        start = landMarkHashMap.get(key).getLatLng();
                        Log.d(TAG, "시작변경" + start);

                        mindist = 0;
                        openMap.put(key, 1);
                        Log.d(TAG, "방문 완료 " + key);

                    }
                }
            }
        }

        for (String key : pathMap.keySet()) {
            Log.d(TAG, "최종경로 키 " + key + " 최종경로값 " + pathMap.get(key).getLatLng());
        }

    }

    public void paintingPath(){

        PolylineOptions options = new PolylineOptions();

        for(String key : pathMap.keySet())
            options.add(pathMap.get(key).getLatLng());

        options.color(Color.RED);
       polyline = map.addPolyline(options);

    }

    public LinkedHashMap<String, LandMark> getResultPath(){
        return pathMap;
    }

    public void deletePath(Polyline polyline){
        polyline.remove();
    }


}

