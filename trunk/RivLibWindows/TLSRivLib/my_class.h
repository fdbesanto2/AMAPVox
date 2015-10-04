#pragma once

#include "riegl/pointcloud.hpp"
#include <iostream>

using namespace std;
using namespace scanlib;

class my_class: public pointcloud
{
	ostream& o_;
	public:
		my_class(ostream& o);

	protected:
		void on_echo_transformed(echo_type echo);

	private:
		unsigned long int shotID; 
};

