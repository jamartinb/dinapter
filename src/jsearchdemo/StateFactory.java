package jsearchdemo;
class StateFactory {

    private GraphRenderer boss;

    private NormalState normal;

    private DraggingState dragging;

    private LinkingState linking;

    private AddingNodeState adding;

    StateFactory(GraphRenderer gr) {
	boss = gr;
	normal = new NormalState(gr);
	dragging = new DraggingState(gr);
	adding = new AddingNodeState(gr);
	linking = new LinkingState(gr);
    }

    State getStartState() {
	return normal;
    }

    State getLinkingState() {
	return linking;
    }

    State getDraggingState() {
	return dragging;
    }

    State getAddingNodeState() {
	return adding;
    }

}



