# FindingPathSample
A* 알고리즘을 변형한 지도상 관광지의 최적 경로를 찾아 그려주는 알고리즘 샘플

마커를 노드로 대응해 그래프 형성.
g(x) + h(x). g(x)는 노드에서 연결된 각 노드당 거리 수치. h(x)는 각 노드에서 목적지까지 일직선 최단거리 값.    
기존 노드간 최단거리를 연결하는 다익스트라 알고리즘의 확장판으로 원래 알고리즘은 한번 탐색 후에 최단경로 노드에서 배제된 노드는 다시 추가하지 않으나 관광지의 단순 최단경로를 구하기 보다는 접근성을 고려하면서 많은 관광지들을 추천해주기 위해  이 기능은 제외되었다.
