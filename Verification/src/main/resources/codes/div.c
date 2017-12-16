PRE {x >= 0 & y >= 0}
	quo := 0;
	rem := x;
	
	WHILE rem >= y DO
		rem := rem - y;
		quo := quo + 1
	OD
POST {quo * y + rem = x & 0 <= rem & rem < y}