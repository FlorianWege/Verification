PRE {x=y}
	x := x + 1;
	PRE {x=y}
		y := y + 1
	POST {x=y};
	y := y + 1
POST {x=y}