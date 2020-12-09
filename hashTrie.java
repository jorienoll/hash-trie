#include <iostream>
#include <vector>
using namespace std;
/**
 *
 * @author jorienoll
 */
class Flight;
class Airport;

vector<Airport*>* airports = new vector<Airport*>();

class Airport{
public:
	int id;
	int heapPos;
	string code;
	vector<Flight>* flights = new vector<Flight>(); //adjacency list
	int parentId; //assigned during calculateShortestPath, used to trace from our end airport to our start airport for shortest route
	int distVal;
	int arrTime; //time we arrive at the airport to calculate for layovers
	Airport(string code);
	Airport();
};

Airport::Airport(){
	this->id = NULL;
	this->parentId = -1;
	this->code = "";
	this->distVal = INT_MAX;
}

Airport::Airport(string code){
	this->id = airports->size();
	this->parentId = -1;
	this->code = code;
	this->distVal = INT_MAX;
}

class Flight{
public:
	Airport* origin;
	Airport* dest;
	int depTime;
	int arrTime;
	int weight;
	Flight(Airport*, Airport*, int, int);
};

Flight::Flight(Airport* origin, Airport* dest, int depTime, int arrTime){
	this->origin = origin;
	this->dest = dest;
	this->depTime = depTime;
	this->arrTime = arrTime;
	this->weight = (arrTime < depTime ? (arrTime + 2400) - depTime : arrTime - depTime); //compensate for passed midnight arrivals
}

class HeapObject{
public:
	Airport* airport;
	int distVal;
	HeapObject(Airport* airport, int distVal);
};

HeapObject::HeapObject(Airport* airport, int distVal){
	this->airport = airport;
	this->distVal = distVal;
}

void insert(vector<HeapObject>* vHeap, HeapObject newObj){
	vHeap->push_back(newObj);
	vHeap->at(vHeap->size() - 1).airport->heapPos = vHeap->size() - 1;
	auto i = vHeap->size() - 1;

	while (i != 0 && vHeap->at((i - 1) / 2).distVal > vHeap->at(i).distVal){
		vHeap->at(i).airport->heapPos = (i - 1) / 2;
		vHeap->at((i - 1) / 2).airport->heapPos = i;
		swap(vHeap->at(i), vHeap->at((i - 1) / 2));
		i = (i - 1) / 2;
	}
}

void heapify(vector<HeapObject>* vHeap, int root){
	auto left = 2 * root + 1;
	auto right = 2 * root + 2;
	int smallest = root;
	if (left < vHeap->size() && vHeap->at(left).distVal < vHeap->at(root).distVal)
		smallest = left;
	if (right < vHeap->size() && vHeap->at(right).distVal < vHeap->at(smallest).distVal)
		smallest = right;
	if (smallest != root){
		vHeap->at(smallest).airport->heapPos = root;
		vHeap->at(root).airport->heapPos = smallest;
		swap(vHeap->at(root), vHeap->at(smallest));
		heapify(vHeap, smallest);
	}
}

HeapObject extractMin(vector<HeapObject>* vHeap){
	if (vHeap->size() == 1){
		auto root = vHeap->at(0);
		vHeap->pop_back();
		return root;
	}

	auto root = vHeap->at(0);
	root.airport->heapPos = NULL;
	vHeap->at(vHeap->size() - 1).airport->heapPos = 0;
	vHeap->at(0) = vHeap->at(vHeap->size() - 1);
	vHeap->pop_back();

	heapify(vHeap, 0);
	return root;
}

void decreaseKey(vector<HeapObject>* vHeap, int i, int value){
	if (vHeap->size() == 0)
		return;

	vHeap->at(i).distVal = value;
	while (i != 0 && vHeap->at((i - 1) / 2).distVal > vHeap->at(i).distVal){
		vHeap->at(i).airport->heapPos = (i - 1) / 2;
		vHeap->at((i - 1) / 2).airport->heapPos = i;
		swap(vHeap->at(i), vHeap->at((i - 1) / 2));
		i = (i - 1) / 2;
	}
}


void processInput(){
	vector<string>* inputAirports = new vector<string>();
	inputAirports->push_back("AEX");
	inputAirports->push_back("BTR");
	inputAirports->push_back("LFT");
	inputAirports->push_back("LCH");
	inputAirports->push_back("MLU");
	inputAirports->push_back("MSY");
	inputAirports->push_back("SHV");

	for (auto i = 0; i < inputAirports->size(); i++){
		airports->push_back(new Airport(inputAirports->at(i)));
	}

	auto inputFlights = vector<Flight>();
	inputFlights.push_back(Flight(airports->at(0), airports->at(1), 800, 900)); //AEX -> BTR
	inputFlights.push_back(Flight(airports->at(1), airports->at(2), 1000, 1100)); //BTR -> LFT
	inputFlights.push_back(Flight(airports->at(2), airports->at(3), 1200, 1300)); //LFT -> LCH
	inputFlights.push_back(Flight(airports->at(3), airports->at(4), 1400, 1500)); //LCH -> MLU
	inputFlights.push_back(Flight(airports->at(4), airports->at(5), 1600, 2300)); //MLU -> MSY
	inputFlights.push_back(Flight(airports->at(5), airports->at(6), 0000, 0200)); //MSY -> SHV
	inputFlights.push_back(Flight(airports->at(0), airports->at(2), 830, 930)); //AEX -> LFT
	inputFlights.push_back(Flight(airports->at(2), airports->at(4), 1030, 1400)); //LFT -> MLU

	for (auto i : inputFlights){
		airports->at(i.origin->id)->flights->push_back(i);
	}
}

void calculateShortestPath(){
	auto heap = new vector<HeapObject>();
	for (auto i = 0; i < airports->size(); i++){
		insert(heap, HeapObject(airports->at(i), airports->at(i)->distVal));
	}

	while (!heap->empty()){
		auto h = extractMin(heap);
		auto hId = h.airport->id;
		auto adjList = airports->at(hId)->flights;
		for (auto i = 0; i < adjList->size(); i++){
			auto flight = adjList->at(i);
			if (h.distVal == 0 && flight.depTime < h.airport->arrTime)
				continue;

			if (h.distVal != 0 && (flight.depTime < h.airport->arrTime ? (flight.depTime + 2400) - h.airport->arrTime : flight.depTime - h.airport->arrTime) < 100)
				continue;

			if (flight.dest->distVal > flight.weight + h.distVal + 100){
				flight.dest->arrTime = flight.arrTime;
				flight.dest->parentId = h.airport->id;
				flight.dest->distVal = flight.weight + h.distVal + 100;
				decreaseKey(heap, flight.dest->heapPos, flight.weight + h.distVal + 100);
			}
		}
	}
}

int main(){
	processInput();
	string startCode = "AEX"; //example starting/ending airports, and startTime
	string endCode = "SHV";
	auto startTime = 800;

	for (auto i = 0; i < airports->size(); i++){
		if (airports->at(i)->code == startCode){
			airports->at(i)->distVal = 0;
			airports->at(i)->arrTime = startTime;
			break;
		}
	}

    calculateShortestPath();

	Airport* currentAirport = new Airport();
	for (auto i = 0; i < airports->size(); i++){
		if (airports->at(i)->code == endCode){
			currentAirport = airports->at(i);
			break;
		}
	}

	auto itinerary = vector<Airport*>();
	while (currentAirport){
		itinerary.push_back(airports->at(currentAirport->id));

		if (currentAirport->parentId == -1 || currentAirport->code == startCode)
			break;
		currentAirport = airports->at(currentAirport->parentId);
	}
	reverse(itinerary.begin(), itinerary.end());

	cout << "Our itinerary is: ";
	for (auto i : itinerary){
		string end = (i->id == itinerary.back()->id) ? "" : " -> ";
		cout << i->code << end;
	}
	cout << endl;
}
