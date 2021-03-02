const path = require("path");
const CleanWebpackPlugin = require("clean-webpack-plugin");

module.exports = {
	mode: process.env.NODE_ENV,
	entry: {
		"wslua-admin": path.resolve(__dirname, "ts/index.ts")
	},
	output: {
		path: path.resolve(__dirname, "../pub/js"),
		filename: "[name].bundle.js",
		library: "WSLua",
		libraryExport: '',
		libraryTarget: 'umd',
		globalObject: 'this'
	},
	resolve: {
		extensions: [".ts", ".js"]
	},
	module: {
		rules: [
			{
				test: /\.ts$/,
				loaders: ["babel-loader", "ts-loader"]
			}
		]
	},
	externals: {
		jquery: 'jQuery'
	},
	plugins: [
		new CleanWebpackPlugin(["dist /ts/*"])
	]
};
