tool
extends Polygon2D

export(Color) var OutLine = Color(0,0,0) setget set_color
export(float) var Width = 2.0 setget set_width
export(bool) var draw = false
func _draw():
	if draw:
		var poly = get_polygon()
		for i in range(1 , poly.size()):
			draw_line(poly[i-1] , poly[i], OutLine , Width)
		draw_line(poly[poly.size() - 1] , poly[0], OutLine , Width)
		
		for i in $"..".red_p_t_d:
			draw_circle(i, 6, Color(1,0,0))
#			print(i)
		for i in $"..".points_to_draw:
			draw_circle(i, 4, Color(0,1,0))

		
		for i in range(10):
			draw_line(Vector2(0, i*100), Vector2(100*10, i*100), Color(0, 0, 0))
			draw_line(Vector2(i*100, 0), Vector2(i*100, 100*10), Color(0, 0, 0))
func set_color(color):
	OutLine = color
	update()

func set_width(new_width):
	Width = new_width
	update()